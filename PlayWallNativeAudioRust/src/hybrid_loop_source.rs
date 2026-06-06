use jni::objects::{Global, JObject, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::{Env, JavaVM, ScopeToken};
use rodio::source::SeekError;
use rodio::{ChannelCount, Decoder, SampleRate, Source};
use std::fs::File;
use std::io::{self, BufReader};
use std::time::Duration;

pub struct HybridLoopSource {
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    jvm: &'static JavaVM,
    java_callback_obj: Global<JObject<'static>>,
    looping_flag_ptr: *const bool,
    start_position: Duration,
    samples_played: u64,
    has_finished: bool,
}

// GlobalRef ist Send, JavaVM ist Send/Sync, daher ist die Source sicher für Rodio
unsafe impl Send for HybridLoopSource {}

impl HybridLoopSource {
    pub fn new(
        path: String,
        looping_flag_ptr: *const bool,
        start_position: Duration,
        jvm: &'static JavaVM,
        java_callback_obj: Global<JObject<'static>>,
    ) -> io::Result<Self> {
        let file = File::open(&path)?;
        let reader = BufReader::new(file);
        let source = Decoder::new(reader)
            .map_err(|e| io::Error::new(io::ErrorKind::InvalidData, e.to_string()))?;

        let mut source: Box<dyn Source<Item=f32> + Send> = Box::new(source);
        let initial_samples = if !start_position.is_zero() {
            source.try_seek(start_position)
                .map_err(|e| io::Error::new(io::ErrorKind::Other, e.to_string()))?;
            let sr = source.sample_rate().get() as f64;
            let ch = source.channels().get() as f64;
            (start_position.as_secs_f64() * sr * ch) as u64
        } else {
            0
        };

        Ok(Self {
            path,
            current_source: source,
            jvm,
            java_callback_obj,
            looping_flag_ptr,
            start_position,
            samples_played: initial_samples,
            has_finished: false,
        })
    }

    fn is_looping_enabled(&self) -> bool {
        unsafe { *self.looping_flag_ptr }
    }

    fn elapsed_seconds(&self) -> f32 {
        self.samples_played as f32
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
}

impl Iterator for HybridLoopSource {
    type Item = f32;

    fn next(&mut self) -> Option<Self::Item> {
        if let Some(sample) = self.current_source.next() {
            self.samples_played += 1;

            // Report progress every 1.000 samples (~20ms at 48kHz Stereo)
            if self.samples_played % 1000 == 0 {
                self.report_progress();
            }

            return Some(sample);
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
                    self.current_source = Box::new(source);
                    self.samples_played = 0;
                    if !self.start_position.is_zero() {
                        let start = self.start_position;
                        let _ = self.try_seek(start);
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
        self.current_source.try_seek(pos)?;
        let sample_rate = self.current_source.sample_rate().get() as f64;
        let channels = self.current_source.channels().get() as f64;
        self.samples_played = (pos.as_secs_f64() * sample_rate * channels) as u64;
        self.has_finished = false;
        Ok(())
    }
}
