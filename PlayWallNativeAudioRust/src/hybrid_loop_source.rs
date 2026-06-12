use jni::objects::{Global, JObject, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::{Env, JavaVM, ScopeToken};
use rodio::source::SeekError;
use rodio::{ChannelCount, Decoder, SampleRate, Source};
use std::fs::File;
use std::io::{self, BufReader};
use crate::time_stretch::WsolaSource;
use std::sync::atomic::{AtomicBool, AtomicU32, Ordering};
use std::sync::Arc;
use std::time::Duration;

pub struct HybridLoopSource {
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    jvm: &'static JavaVM,
    java_callback_obj: Global<JObject<'static>>,
    looping: Arc<AtomicBool>,
    speed: Arc<AtomicU32>,
    start_position: Duration,
    end_position_samples: Option<u64>,
    // Real-time output sample counter — used only for the progress-report interval.
    samples_played: u64,
    // Audio-time sample accumulator: incremented by `speed` per output sample so that
    // elapsed_seconds() always returns the position in the audio file, not wall-clock time.
    // Consistent with end_position_samples (both in audio-sample units) and with seekTo()
    // (which operates in audio time), so speed changes never affect correctness.
    audio_position_samples: f64,
    has_finished: bool,
}

// GlobalRef ist Send, JavaVM ist Send/Sync, daher ist die Source sicher für Rodio
unsafe impl Send for HybridLoopSource {}

fn duration_to_samples(d: Duration, sample_rate: f64, channels: f64) -> u64 {
    (d.as_secs_f64() * sample_rate * channels) as u64
}

impl HybridLoopSource {
    pub fn new(
        path: String,
        looping: Arc<AtomicBool>,
        speed: Arc<AtomicU32>,
        start_position: Duration,
        end_position: Option<Duration>,
        jvm: &'static JavaVM,
        java_callback_obj: Global<JObject<'static>>,
    ) -> io::Result<Self> {
        let file = File::open(&path)?;
        let reader = BufReader::new(file);
        let mut decoder = Decoder::new(reader)
            .map_err(|e| io::Error::new(io::ErrorKind::InvalidData, e.to_string()))?;

        let sample_rate = decoder.sample_rate().get() as f64;
        let channels = decoder.channels().get() as f64;

        let end_position_samples = end_position.map(|pos| duration_to_samples(pos, sample_rate, channels));

        let initial_samples = if !start_position.is_zero() {
            decoder.try_seek(start_position)
                .map_err(|e| io::Error::new(io::ErrorKind::Other, e.to_string()))?;
            duration_to_samples(start_position, sample_rate, channels)
        } else {
            0
        };

        let current_source: Box<dyn Source<Item=f32> + Send> =
            Box::new(WsolaSource::new(decoder, Arc::clone(&speed)));

        Ok(Self {
            path,
            current_source,
            jvm,
            java_callback_obj,
            looping,
            speed,
            start_position,
            end_position_samples,
            samples_played: initial_samples,
            audio_position_samples: initial_samples as f64,
            has_finished: false,
        })
    }

    fn is_looping_enabled(&self) -> bool {
        self.looping.load(Ordering::Acquire)
    }

    fn is_end_reached(&self) -> bool {
        self.end_position_samples
            .map_or(false, |end| self.audio_position_samples >= end as f64)
    }

    fn elapsed_seconds(&self) -> f32 {
        self.audio_position_samples as f32
            / (self.current_source.sample_rate().get() as f32 * self.current_source.channels().get() as f32)
    }

    fn with_env<R>(&self, f: impl FnOnce(&mut Env) -> R) -> R {
        let mut token = ScopeToken::default();

        if let Ok(mut guard) = unsafe { self.jvm.get_env_attachment(&mut token) } {
            return f(guard.borrow_env_mut());
        }

        self.jvm
            .attach_current_thread(|env| Ok::<R, jni::errors::Error>(f(env)))
            .ok()
            .unwrap()
    }

    fn report_progress(&self) {
        self.with_env(|env| {
            let param = &RuntimeMethodSignature::from_str("(D)V").unwrap();
            let sig = MethodSignature::from(param);

            let _ = env.call_method(
                &self.java_callback_obj,
                JNIString::new("onProgress"),
                sig,
                &[JValue::Double(self.elapsed_seconds() as f64)],
            );
        });
    }

    fn report_eof(&self) {
        self.with_env(|env| {
            let param = &RuntimeMethodSignature::from_str("()V").unwrap();
            let sig = MethodSignature::from(param);
            let _ = env.call_method(&self.java_callback_obj, JNIString::new("onEof"), sig, &[]);
        });
    }

    fn report_error_as_exception(&self, exception_class: &'static str, message: &str) {
        self.with_env(|env| {
            let Ok(class) = env.find_class(JNIString::new(exception_class)) else { return; };
            let Ok(java_msg) = env.new_string(message) else { return; };
            let constructor_param = &RuntimeMethodSignature::from_str("(Ljava/lang/String;)V").unwrap();
            let Ok(exception_obj) = env.new_object(&class, MethodSignature::from(constructor_param), &[JValue::Object(&java_msg)]) else { return; };
            let method_param = &RuntimeMethodSignature::from_str("(Ljava/lang/Throwable;)V").unwrap();
            let _ = env.call_method(
                &self.java_callback_obj,
                JNIString::new("onError"),
                MethodSignature::from(method_param),
                &[JValue::Object(&exception_obj)],
            );
        });
    }

    fn rebuild_source(&mut self) -> Result<(), SeekError> {
        let file = File::open(&self.path)
            .map_err(|e| SeekError::Other(Arc::new(io::Error::new(
                io::ErrorKind::NotFound,
                format!("{}: {}", self.path, e),
            ))))?;

        let decoder = Decoder::new(BufReader::new(file))
            .map_err(|e| SeekError::Other(Arc::new(io::Error::new(
                io::ErrorKind::InvalidData,
                e.to_string(),
            ))))?;

        self.current_source = Box::new(WsolaSource::new(decoder, Arc::clone(&self.speed)));
        Ok(())
    }
}

impl Iterator for HybridLoopSource {
    type Item = f32;

    fn next(&mut self) -> Option<Self::Item> {
        let end_reached = self.is_end_reached();

        if !end_reached {
            if let Some(sample) = self.current_source.next() {
                self.samples_played += 1;
                let speed = f32::from_bits(self.speed.load(Ordering::Relaxed));
                self.audio_position_samples += speed as f64;

                // Report progress every 1.000 samples (~20ms at 48kHz Stereo)
                if self.samples_played % 1000 == 0 {
                    self.report_progress();
                }

                return Some(sample);
            }
        }

        if self.is_looping_enabled() {
            let source = File::open(&self.path)
                .map_err(|e| (format!("{}: {}", self.path, e), "java/io/FileNotFoundException"))
                .and_then(|file| {
                    Decoder::new(BufReader::new(file))
                        .map_err(|e| (format!("{}: {}", self.path, e), "java/io/IOException"))
                });

            return match source {
                Ok(source) => {
                    self.current_source = Box::new(WsolaSource::new(source, Arc::clone(&self.speed)));
                    self.samples_played = 0;
                    self.audio_position_samples = 0.0;
                    if !self.start_position.is_zero() {
                        let start = self.start_position;
                        if let Err(e) = self.try_seek(start) {
                            if !self.has_finished {
                                self.report_error_as_exception(
                                    "java/io/IOException",
                                    &format!("Failed to seek to start position on loop restart: {}", e),
                                );
                                self.has_finished = true;
                            }
                            return None;
                        }
                    }
                    // Guard: if end <= start, avoid infinite recursion
                    if self.is_end_reached() {
                        if !self.has_finished {
                            self.report_eof();
                            self.has_finished = true;
                        }
                        return None;
                    }
                    self.next()
                }
                Err((err_msg, exception_class)) => {
                    if !self.has_finished {
                        self.report_error_as_exception(exception_class, &err_msg);
                        self.has_finished = true;
                    }
                    None
                }
            }
        }

        if !self.has_finished {
            self.report_eof();
            self.has_finished = true;
        }

        None
    }
}

impl Source for HybridLoopSource {
    fn current_span_len(&self) -> Option<usize> {
        self.current_source.current_span_len()
    }

    fn channels(&self) -> ChannelCount {
        self.current_source.channels()
    }

    fn sample_rate(&self) -> SampleRate {
        self.current_source.sample_rate()
    }

    fn total_duration(&self) -> Option<Duration> {
        None
    }

    fn try_seek(&mut self, pos: Duration) -> Result<(), SeekError> {
        let sample_rate = self.current_source.sample_rate().get() as f64;
        let channels = self.current_source.channels().get() as f64;
        let pos_samples_after_seek = duration_to_samples(pos, sample_rate, channels);

        if let Some(end_samples) = self.end_position_samples {
            if pos_samples_after_seek >= end_samples {
                return Err(SeekError::Other(Arc::new(io::Error::new(
                    io::ErrorKind::InvalidInput,
                    format!(
                        "Seek position {:.3}s is at or beyond the configured end position",
                        pos.as_secs_f64()
                    ),
                ))));
            }
        }

        #[cfg(target_os = "windows")]
        if pos_samples_after_seek < self.audio_position_samples as u64 {
            self.rebuild_source()?;
        }

        self.current_source.try_seek(pos)?;
        self.samples_played = pos_samples_after_seek;
        self.audio_position_samples = pos_samples_after_seek as f64;
        self.has_finished = false;
        Ok(())
    }
}
