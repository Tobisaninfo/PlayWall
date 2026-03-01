use jni::objects::JValue;
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::sys::jint;
use tracing::{Event, Subscriber};
use tracing_subscriber::{Layer, layer::Context};

pub struct JavaLayer;

impl<S> Layer<S> for JavaLayer
where
    S: Subscriber,
{
    fn on_event(&self, event: &Event, _ctx: Context<S>) {
        let mut msg = String::new();

        use tracing::field::{Field, Visit};
        struct Visitor<'a>(&'a mut String);

        impl<'a> Visit for Visitor<'a> {
            fn record_debug(&mut self, _: &Field, value: &dyn std::fmt::Debug) {
                self.0.push_str(&format!("{:?}", value));
            }
        }

        event.record(&mut Visitor(&mut msg));

        let level = level_to_int(*event.metadata().level());

        let jvm: &'static jni::JavaVM = unsafe {
            let jvm_lock = crate::JVM.read().unwrap();
            let jvm_ref = jvm_lock.as_ref().expect("JVM not initialized");
            &*(jvm_ref as *const jni::JavaVM)
        };

        jvm.attach_current_thread(|env| {
            let jmsg = env.new_string(msg).unwrap();
            let class = env
                .find_class(JNIString::new(
                    "de/tobias/playwall/nativeaudio/audio/rust/RustLogger",
                ))
                .unwrap();

            let param = &RuntimeMethodSignature::from_str("(ILjava/lang/String;)V").unwrap();
            let sig = MethodSignature::from(param);

            env.call_static_method(
                class,
                JNIString::new("logFromRust"),
                sig,
                &[JValue::Int(level), JValue::Object(&jmsg)],
            )
                .unwrap();

            Ok::<(), jni::errors::Error>(())
        })
            .ok()
            .unwrap();
    }
}

fn level_to_int(level: tracing::Level) -> jint {
    match level {
        tracing::Level::ERROR => 1,
        tracing::Level::WARN => 2,
        tracing::Level::INFO => 3,
        tracing::Level::DEBUG => 4,
        tracing::Level::TRACE => 5,
    }
}
