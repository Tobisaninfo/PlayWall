#![allow(non_snake_case)]

mod hybrid_loop_source;
mod logger;
mod output_devices;
mod playback;

use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::signature::{FieldSignature, RuntimeFieldSignature};
use jni::strings::JNIString;
use jni::sys::{jboolean, jint, jlong};
use jni::{Env, EnvUnowned, JavaVM};
use lazy_static::lazy_static;
use rodio::{MixerDeviceSink, Player};
use std::fs::File;
use std::sync::RwLock;
use symphonia::core::io::MediaSourceStream;
use symphonia::core::probe::Hint;
use symphonia::default::get_probe;
use tracing::{debug, trace};
use tracing_subscriber;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use tracing_subscriber::{EnvFilter, fmt};
use crate::logger::JavaLayer;

struct AudioHandler {
    media_path: Option<String>,
    duration: Option<f64>,
    audio_stream_handler: Option<AudioStreamHandler>,
    device_name: Option<String>,
    volume: f32,
    looping: bool,
}

impl AudioHandler {
    fn new() -> Self {
        Self {
            media_path: None,
            duration: None,
            audio_stream_handler: None,
            device_name: None,
            volume: 1.0,
            looping: false,
        }
    }

    fn setMedia(&mut self, path: String, duration: f64) {
        self.media_path = Some(path);
        self.duration = Some(duration);
    }

    fn clearMedia(&mut self) {
        self.media_path = None;
        self.duration = None;
    }

    fn setAudioHandlerStream(&mut self, audio_stream_handler: AudioStreamHandler) {
        self.audio_stream_handler = Some(audio_stream_handler);
    }
}

#[allow(dead_code)]
struct AudioStreamHandler {
    stream_handler: MixerDeviceSink,
    sink: Player,
}

lazy_static! {
    static ref JVM: RwLock<Option<JavaVM>> = RwLock::new(None);
}

#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(
    vm: *mut jni::sys::JavaVM,
    _reserved: *mut std::ffi::c_void,
) -> jint {
    let vm = unsafe { JavaVM::from_raw(vm) };
    let mut guard = JVM.write().unwrap();
    *guard = Some(vm);
    jni::sys::JNI_VERSION_1_8
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_initSystem(
    mut env: EnvUnowned,
    _class: JClass,
    logLevel: JString,
) {
    let _ = env.with_env(|_| {
        let log_level_str: String = logLevel.to_string();
        let filter = EnvFilter::new(log_level_str).add_directive("jni=warn".parse().unwrap());

        let subscriber = tracing_subscriber::registry()
            .with(filter)
            // .with(fmt::layer())
            .with(JavaLayer);

        let _ = subscriber.try_init();

        debug!("Initialized rust audio component");
        Ok::<(), jni::errors::Error>(())
    });
}

const NATIVE_POINTER_FIELD_NAME: &'static str = "nativePointer";
const NATIVE_POINTER_FIELD_TYPE: &'static str = "J";

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_createNativeInstance(
    mut env: EnvUnowned,
    this: JObject,
) {
    debug!("Creating native instance");
    let _ = env.with_env(|env| {
        if (env
            .get_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            )
            .unwrap()
            .j()
            .unwrap())
            != 0
        {
            env.throw_new(
                JNIString::new("java/lang/IllegalStateException"),
                JNIString::new("Native instance already created"),
            )?;
            return Ok::<(), jni::errors::Error>(());
        }

        let audio_handler = Box::new(AudioHandler::new());
        let ptr = Box::into_raw(audio_handler) as jlong;

        env.set_field(
            &this,
            JNIString::new(NATIVE_POINTER_FIELD_NAME),
            FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            JValue::Long(ptr),
        )
            .unwrap();
        trace!("Created audio_handler with ptr: {}", ptr);
        Ok::<(), jni::errors::Error>(())
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_destroy(
    mut env: EnvUnowned,
    this: JObject,
) {
    let _ = env.with_env(|env| {
        let ptr = env
            .get_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            )
            .unwrap()
            .j()
            .unwrap();

        if ptr != 0 {
            unsafe {
                drop(Box::from_raw(ptr as *mut AudioHandler));
            }
            env.set_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
                JValue::Long(0),
            )
                .unwrap();
            trace!("Destroyed audio_handler with ptr: {}", ptr);
        }
        Ok::<(), jni::errors::Error>(())
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_loadMediaNative(
    mut env: EnvUnowned,
    obj: JObject,
    path: JString,
) {
    let _ = env.with_env(|mut env| {
        let path_str: String = path.to_string();
        debug!("Load path: {}", &path_str);

        if let Ok(file) = File::open(&path_str) {
            let media_source_stream = MediaSourceStream::new(Box::new(file), Default::default());
            let hint = Hint::new();
            let probed = get_probe()
                .format(
                    &hint,
                    media_source_stream,
                    &Default::default(),
                    &Default::default(),
                )
                .unwrap();
            let format = probed.format;

            let track = format.default_track().unwrap();
            let params = &track.codec_params;

            let duration_seconds;
            if let (Some(sample_rate), Some(n_frames)) = (params.sample_rate, params.n_frames) {
                duration_seconds = n_frames as f64 / sample_rate as f64;
            } else {
                duration_seconds = 0.0;
            }
            with_audio_handler(&mut env, obj, |_env, audio_handler| {
                audio_handler.setMedia(path_str, duration_seconds);
                trace!("Loaded media");
            });
        } else {
            env.throw_new(
                JNIString::new("java/io/FileNotFoundException"),
                JNIString::new(format!("File not found: {}", &path_str)),
            )
                .unwrap();
        }

        Ok::<(), jni::errors::Error>(())
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_unloadMediaNative(
    mut env: EnvUnowned,
    obj: JObject,
) {
    let _ = env.with_env(|mut env| {
        with_audio_handler(&mut env, obj, |_env, audio_handler| {
            audio_handler.clearMedia();
            trace!("Unload media");
        });

        Ok::<(), jni::errors::Error>(())
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_isMediaLoadedNative(
    mut unowned_env: EnvUnowned,
    obj: JObject,
) -> jboolean {
    unowned_env
        .with_env(|env| -> jni::errors::Result<jboolean> {
            let is_loaded = with_audio_handler(env, obj, |_env, audio_handler| {
                trace!("Check if media is loaded");
                audio_handler.media_path.is_some()
            })
                .unwrap();
            Ok(is_loaded as jboolean)
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

fn with_audio_handler<T>(
    env: &mut Env,
    this: JObject,
    f: impl FnOnce(&mut Env, &mut AudioHandler) -> T,
) -> Option<T> {
    let ptr = env
        .get_field(
            &this,
            JNIString::new(NATIVE_POINTER_FIELD_NAME),
            FieldSignature::from(
                &RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE).unwrap(),
            ),
        )
        .unwrap()
        .j()
        .unwrap();
    if ptr == 0 {
        env.throw_new(
            JNIString::new("java/lang/IllegalStateException"),
            JNIString::new("audio_handler not initialized"),
        )
            .unwrap();
        None
    } else {
        let audio_handler: &mut AudioHandler = unsafe { &mut *(ptr as *mut AudioHandler) };
        Some(f(env, audio_handler))
    }
}
