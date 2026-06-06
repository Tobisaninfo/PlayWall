use jni::objects::{Global, JObject, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::{Env, JavaVM, ScopeToken};
use rodio::source::SeekError;
use rodio::{ChannelCount, Decoder, SampleRate, Source};
use std::fs::File;
use std::io::{self, BufReader};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::time::Duration;

pub struct HybridLoopSource {
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    jvm: &'static JavaVM,
    java_callback_obj: Global<JObject<'static>>,
    looping_flag_ptr: *const bool,
    seek_to_start_flag: Arc<AtomicBool>,
    samples_played: u64,
    has_finished: bool,
}

// GlobalRef ist Send, JavaVM ist Send/Sync, daher ist die Source sicher für Rodio
unsafe impl Send for HybridLoopSource {}

impl HybridLoopSource {
    pub fn new(
        path: String,
        looping_flag_ptr: *const bool,
        seek_to_start_flag: Arc<AtomicBool>,
        jvm: &'static JavaVM,
        java_callback_obj: Global<JObject<'static>>,
    ) -> io::Result<Self> {
        let source = Self::open_decoder(&path)?;

        Ok(Self {
            path,
            current_source: Box::new(source),
            jvm,
            java_callback_obj,
            looping_flag_ptr,
            seek_to_start_flag,
            samples_played: 0,
            has_finished: false,
        })
    }

    fn open_decoder(path: &str) -> io::Result<Decoder<BufReader<File>>> {
        let file = File::open(path)?;
        let reader = BufReader::new(file);
        Decoder::new(reader).map_err(|e| io::Error::new(io::ErrorKind::InvalidData, e.to_string()))
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
        if self.seek_to_start_flag.swap(false, Ordering::AcqRel) {
            match Self::open_decoder(&self.path) {
                Ok(source) => {
                    self.current_source = Box::new(source);
                    self.samples_played = 0;
                    self.has_finished = false;
                }
                Err(e) => {
                    if !self.has_finished {
                        self.report_error_as_exception("java/io/IOException", &format!("{}: {}", self.path, e));
                        self.has_finished = true;
                    }
                    return None;
                }
            }
        }

        if let Some(sample) = self.current_source.next() {
            self.samples_played += 1;

            // Report progress every 1.000 samples (~20ms at 48kHz Stereo)
            if self.samples_played % 1000 == 0 {
                self.report_progress();
            }

            return Some(sample);
        }

        if self.is_looping_enabled() {
            return match Self::open_decoder(&self.path) {
                Ok(source) => {
                    self.current_source = Box::new(source);
                    self.samples_played = 0;
                    self.next()
                }
                Err(e) => {
                    if !self.has_finished {
                        self.report_error_as_exception("java/io/IOException", &format!("{}: {}", self.path, e));
                        self.has_finished = true;
                    }
                    None
                }
            };
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
