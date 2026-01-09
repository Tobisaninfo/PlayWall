#![allow(non_snake_case)]

mod output_devices;
mod playback;
mod looping_source;
mod eof_callback_source;
mod hybrid_loop_source;

use jni::objects::{JClass, JObject, JString, JValue};
use jni::sys::{jboolean, jlong};
use jni::{JNIEnv, JavaVM};
use lazy_static::lazy_static;
use rodio::{OutputStream, Sink};
use std::fs::File;
use std::str::FromStr;
use std::sync::RwLock;
use symphonia::core::io::MediaSourceStream;
use symphonia::core::probe::Hint;
use symphonia::default::get_probe;
use tracing::{debug, trace};
use tracing_subscriber;

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
    stream_handler: OutputStream,
    sink: Sink,
}

lazy_static! {
    static ref JVM: RwLock<Option<JavaVM>> = RwLock::new(None);
}

#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _reserved: *mut std::ffi::c_void) -> jni::sys::jint {
    let mut guard = JVM.write().unwrap();
    *guard = Some(vm);
    jni::sys::JNI_VERSION_1_8
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_initSystem(
    mut env: JNIEnv,
    _class: JClass,
    logLevel: JString,
) {
    let log_level_str: String = env
        .get_string(&logLevel)
        .expect("Couldn't get java string")
        .into();

    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::from_str(&log_level_str).unwrap())
        .init();
    debug!("Initialized rust audio component");
}

const NATIVE_POINTER_FIELD_NAME: &'static str = "nativePointer";
const NATIVE_POINTER_FIELD_TYPE: &'static str = "J";

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_createNativeInstance(
    mut env: JNIEnv,
    this: JObject,
) {
    if (env
        .get_field(&this, NATIVE_POINTER_FIELD_NAME, NATIVE_POINTER_FIELD_TYPE)
        .unwrap()
        .j()
        .unwrap())
        != 0
    {
        env.throw_new(
            "java/lang/IllegalStateException",
            "Native instance already created",
        )
        .unwrap();
        return;
    }

    let audio_handler = Box::new(AudioHandler::new());
    let ptr = Box::into_raw(audio_handler) as jlong;

    env.set_field(
        &this,
        NATIVE_POINTER_FIELD_NAME,
        NATIVE_POINTER_FIELD_TYPE,
        JValue::Long(ptr),
    )
    .unwrap();
    trace!("Created audio_handler with ptr: {}", ptr);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_destroy(
    mut env: JNIEnv,
    this: JObject,
) {
    let ptr = env
        .get_field(&this, NATIVE_POINTER_FIELD_NAME, NATIVE_POINTER_FIELD_TYPE)
        .unwrap()
        .j()
        .unwrap();

    if ptr != 0 {
        unsafe {
            drop(Box::from_raw(ptr as *mut AudioHandler));
        }
        env.set_field(
            &this,
            NATIVE_POINTER_FIELD_NAME,
            NATIVE_POINTER_FIELD_TYPE,
            JValue::Long(0),
        )
        .unwrap();
        trace!("Destroyed audio_handler with ptr: {}", ptr);
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_loadMediaNative(
    mut env: JNIEnv,
    obj: JObject,
    path: JString,
) {
    let path_str: String = env
        .get_string(&path)
        .expect("Couldn't get java string")
        .into();
    debug!("Load path: {}", &path_str);

    if let Ok(file) = File::open(&path_str) {
        let media_source_stream = MediaSourceStream::new(Box::new(file), Default::default());
        let hint = Hint::new();
        let probed = get_probe()
            .format(&hint, media_source_stream, &Default::default(), &Default::default())
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
            "java/io/FileNotFoundException",
            format!("File not found: {}", &path_str),
        )
        .unwrap();
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_unloadMediaNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        audio_handler.clearMedia();
        trace!("Unload media");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_isMediaLoadedNative(
    mut env: JNIEnv,
    obj: JObject,
) -> jboolean {
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        trace!("Unload media");
        return audio_handler.media_path.is_some() as jboolean;
    })
    .unwrap()
}

fn with_audio_handler<T>(
    env: &mut JNIEnv,
    this: JObject,
    f: impl FnOnce(&mut JNIEnv, &mut AudioHandler) -> T,
) -> Option<T> {
    let ptr = env
        .get_field(this, NATIVE_POINTER_FIELD_NAME, NATIVE_POINTER_FIELD_TYPE)
        .unwrap()
        .j()
        .unwrap();
    if ptr == 0 {
        env.throw_new("java/lang/IllegalStateException", "audio_handler not initialized")
            .unwrap();
        None
    } else {
        let audio_handler: &mut AudioHandler = unsafe { &mut *(ptr as *mut AudioHandler) };
        Some(f(env, audio_handler))
    }
}
