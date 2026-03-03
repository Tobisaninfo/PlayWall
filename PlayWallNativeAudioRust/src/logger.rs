use jni::objects::{JClass, JStaticMethodID, JString, JValue};
use jni::refs::Global;
use jni::signature::{MethodSignature, Primitive, ReturnType, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::sys::jint;
use std::sync::OnceLock;
use tracing::field::{Field, Visit};
use tracing::{Event, Subscriber};
use tracing_subscriber::{layer::Context, Layer};

struct MethodCache {
    class_global: Global<JClass<'static>>,
    method_id: JStaticMethodID,
}

unsafe impl Send for MethodCache {}
unsafe impl Sync for MethodCache {}

static CACHE: OnceLock<MethodCache> = OnceLock::new();

pub struct JavaLayer;

impl<S> Layer<S> for JavaLayer
where
    S: Subscriber,
{
    fn on_event(&self, event: &Event, _ctx: Context<S>) {
        let mut msg = String::new();

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
            let cache = CACHE.get_or_init(|| {
                let local_class: JClass = env
                    .find_class(JNIString::new(
                        "de/tobias/playwall/nativeaudio/audio/rust/RustLogger",
                    ))
                    .expect("Failed to find RustLogger class");

                let class_global: Global<JClass<'static>> = env
                    .new_global_ref(local_class)
                    .expect("Failed to create global ref");

                let param =
                    &RuntimeMethodSignature::from_str("(ILjava/lang/String;)V").unwrap();
                let sig = MethodSignature::from(param);

                let method_id: JStaticMethodID = env
                    .get_static_method_id(&class_global, JNIString::new("logFromRust"), sig)
                    .expect("Failed to get method ID");

                MethodCache {
                    class_global,
                    method_id,
                }
            });

            let jmsg: JString = env.new_string(msg).unwrap();

            unsafe {
                env.call_static_method_unchecked(
                    &cache.class_global,
                    cache.method_id,
                    ReturnType::Primitive(Primitive::Void),
                    &[JValue::Int(level).as_jni(), JValue::Object(&jmsg).as_jni()],
                )
                    .unwrap();
            }

            Ok::<(), jni::errors::Error>(())
        })
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
