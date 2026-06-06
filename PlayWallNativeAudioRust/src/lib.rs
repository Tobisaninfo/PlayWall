#![allow(non_snake_case)]

mod hybrid_loop_source;
mod logger;
mod output_devices;
mod playback;

use crate::logger::JavaLayer;
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
use tracing_subscriber::EnvFilter;

struct AudioHandler {
    media_path: Option<String>,
    duration: Option<f64>,
    audio_stream_handler: Option<AudioStreamHandler>,
    device_name: Option<String>,
    volume: f32,
    looping: bool,
    start_position_secs: f64,
    end_position_secs: Option<f64>,
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
            start_position_secs: 0.0,
            end_position_secs: None,
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

    fn clearAudioHandlerStream(&mut self) {
        self.audio_stream_handler = None;
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
    env.with_env(|_| -> jni::errors::Result<()> {
        let log_level_str: String = logLevel.to_string();
        let filter = EnvFilter::new(log_level_str).add_directive("jni=warn".parse().unwrap());

        let subscriber = tracing_subscriber::registry()
            .with(filter)
            // .with(fmt::layer())
            .with(JavaLayer);

        let _ = subscriber.try_init();

        debug!("Initialized rust audio component");
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

const NATIVE_POINTER_FIELD_NAME: &'static str = "nativePointer";
const NATIVE_POINTER_FIELD_TYPE: &'static str = "J";

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_createNativeInstance(
    mut env: EnvUnowned,
    this: JObject,
) {
    debug!("Creating native instance");
    env.with_env(|env| -> jni::errors::Result<()> {
        if env
            .get_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            )?
            .j()?
            != 0
        {
            env.throw_new(
                JNIString::new("java/lang/IllegalStateException"),
                JNIString::new("Native instance already created"),
            )?;
            return Ok(());
        }

        let audio_handler = Box::new(AudioHandler::new());
        let ptr = Box::into_raw(audio_handler) as jlong;

        env.set_field(
            &this,
            JNIString::new(NATIVE_POINTER_FIELD_NAME),
            FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            JValue::Long(ptr),
        )?;
        trace!("Created audio_handler with ptr: {}", ptr);
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_destroy(
    mut env: EnvUnowned,
    this: JObject,
) {
    env.with_env(|env| -> jni::errors::Result<()> {
        let ptr = env
            .get_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
            )?
            .j()?;

        if ptr != 0 {
            unsafe {
                drop(Box::from_raw(ptr as *mut AudioHandler));
            }
            env.set_field(
                &this,
                JNIString::new(NATIVE_POINTER_FIELD_NAME),
                FieldSignature::from(&RuntimeFieldSignature::from_str(NATIVE_POINTER_FIELD_TYPE)?),
                JValue::Long(0),
            )?;
            trace!("Destroyed audio_handler with ptr: {}", ptr);
        }
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_loadMediaNative(
    mut env: EnvUnowned,
    obj: JObject,
    path: JString,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        let path_str: String = path.to_string();
        debug!("Load path: {}", &path_str);

        match File::open(&path_str) {
            Ok(file) => {
                let media_source_stream = MediaSourceStream::new(Box::new(file), Default::default());
                let hint = Hint::new();
                let probed = match get_probe().format(
                    &hint,
                    media_source_stream,
                    &Default::default(),
                    &Default::default(),
                ) {
                    Ok(p) => p,
                    Err(e) => {
                        env.throw_new(
                            JNIString::new("java/io/IOException"),
                            JNIString::new(format!("Failed to probe media format: {}", e)),
                        )?;
                        return Ok(());
                    }
                };
                let format = probed.format;

                let track = match format.default_track() {
                    Some(t) => t,
                    None => {
                        env.throw_new(
                            JNIString::new("java/io/IOException"),
                            JNIString::new("No audio track found in media file"),
                        )?;
                        return Ok(());
                    }
                };
                let params = &track.codec_params;

                let duration_seconds = if let (Some(sample_rate), Some(n_frames)) = (params.sample_rate, params.n_frames) {
                    n_frames as f64 / sample_rate as f64
                } else {
                    0.0
                };

                with_audio_handler(&mut env, obj, |_env, audio_handler| {
                    audio_handler.setMedia(path_str, duration_seconds);
                    trace!("Loaded media");
                });
            }
            Err(err) => {
                env.throw_new(
                    JNIString::new("java/io/FileNotFoundException"),
                    JNIString::new(format!("{}: {}", path_str, err.to_string())),
                )?;
            }
        }

        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_unloadMediaNative(
    mut env: EnvUnowned,
    obj: JObject,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        with_audio_handler(&mut env, obj, |_env, audio_handler| {
            audio_handler.clearMedia();
            trace!("Unload media");
        });

        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
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
            .ok();
        None
    } else {
        let audio_handler: &mut AudioHandler = unsafe { &mut *(ptr as *mut AudioHandler) };
        Some(f(env, audio_handler))
    }
}
