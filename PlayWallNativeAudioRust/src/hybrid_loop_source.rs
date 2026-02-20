use jni::objects::{Global, JObject, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::{Env, JavaVM, ScopeToken};
use rodio::{Decoder, Source};
use std::fs::File;
use std::io::BufReader;
use std::time::Duration;

pub struct HybridLoopSource {
    path: String,
    current_source: Box<dyn Source<Item=f32> + Send>,
    jvm: &'static JavaVM,
    java_callback_obj: Global<JObject<'static>>,
    looping_flag_ptr: *const bool,
    samples_played: u64,
    has_finished: bool,
}

// GlobalRef ist Send, JavaVM ist Send/Sync, daher ist die Source sicher für Rodio
unsafe impl Send for HybridLoopSource {}

impl HybridLoopSource {
    pub fn new(
        path: String,
        looping_flag_ptr: *const bool,
        jvm: &'static JavaVM,
        java_callback_obj: Global<JObject<'static>>,
    ) -> Self {
        let file = File::open(&path).expect("Failed to open file");
        let reader = BufReader::new(file);
        let source = Decoder::new(reader).expect("Failed to create decoder");

        Self {
            path,
            current_source: Box::new(source),
            jvm,
            java_callback_obj,
            looping_flag_ptr,
            samples_played: 0,
            has_finished: false,
        }
    }

    fn is_looping_enabled(&self) -> bool {
        unsafe { *self.looping_flag_ptr }
    }

    fn elapsed_seconds(&self) -> f32 {
        self.samples_played as f32
            / (self.current_source.sample_rate() as f32 * self.current_source.channels() as f32)
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
            let file = File::open(&self.path).ok()?;
            let reader = BufReader::new(file);
            if let Ok(source) = Decoder::new(reader) {
                self.current_source = Box::new(source);
                self.samples_played = 0;
                return self.next();
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

    fn channels(&self) -> u16 {
        self.current_source.channels()
    }

    fn sample_rate(&self) -> u32 {
        self.current_source.sample_rate()
    }

    fn total_duration(&self) -> Option<Duration> {
        None
    }
}
