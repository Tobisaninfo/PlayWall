#![allow(non_snake_case)]

mod output_devices;
mod playback;

use jni::objects::{GlobalRef, JClass, JObject, JString, JValue};
use jni::sys::{jboolean, jlong, jstring};
use jni::{JNIEnv, JavaVM};
use rodio::source::Buffered;
use rodio::{Decoder, OutputStream, Sink, Source};
use std::fs::File;
use std::io::BufReader;
use std::str::FromStr;
use symphonia::core::io::MediaSourceStream;
use symphonia::core::probe::Hint;
use symphonia::default::get_probe;
use tracing::{debug, trace};
use tracing_subscriber;

struct RustBridge {
    java_obj: GlobalRef,
    jvm: JavaVM,
    source: Option<Buffered<Decoder<BufReader<File>>>>,
    duration: Option<f64>,
    audio_stream_handler: Option<AudioStreamHandler>,
}

impl RustBridge {
    fn new(java_obj: GlobalRef, jvm: JavaVM) -> Self {
        Self {
            java_obj,
            jvm,
            source: None,
            duration: None,
            audio_stream_handler: None,
        }
    }

    fn setSource(&mut self, source: Buffered<Decoder<BufReader<File>>>, duration: f64) {
        self.source = Some(source);
        self.duration = Some(duration);
    }

    fn clearSource(&mut self) {
        self.source = None;
    }

    fn setAudioHandlerStream(&mut self, audio_stream_handler: AudioStreamHandler) {
        self.audio_stream_handler = Some(audio_stream_handler);
    }
}

struct AudioStreamHandler {
    stream_handler: OutputStream,
    sink: Sink,
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_initSystem(
    mut env: JNIEnv,
    _class: JClass,
    logLevel: jstring,
) {
    unsafe {
        let j_string = JString::from_raw(logLevel);
        let log_level_str: String = env
            .get_string(&j_string)
            .expect("Couldn't get java string")
            .into();

        tracing_subscriber::fmt()
            .with_max_level(tracing::Level::from_str(&log_level_str).unwrap())
            .init();
        debug!("Initialized rust audio component");
    }
}

const NATIVE_POINTER_FIELD_NAME: &'static str = "nativePointer";
const NATIVE_POINTER_FIELD_TYPE: &'static str = "J";

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_createNativeInstance(
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

    let global_ref = env.new_global_ref(&this).unwrap();
    let jvm = env.get_java_vm().unwrap();

    let bridge = Box::new(RustBridge::new(global_ref, jvm));
    let ptr = Box::into_raw(bridge) as jlong;

    env.set_field(
        &this,
        NATIVE_POINTER_FIELD_NAME,
        NATIVE_POINTER_FIELD_TYPE,
        JValue::Long(ptr),
    )
    .unwrap();
    trace!("Created bridge with ptr: {}", ptr);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_destroy(
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
            drop(Box::from_raw(ptr as *mut RustBridge));
        }
        env.set_field(
            &this,
            NATIVE_POINTER_FIELD_NAME,
            NATIVE_POINTER_FIELD_TYPE,
            JValue::Long(0),
        )
        .unwrap();
        trace!("Destroyed bridge with ptr: {}", ptr);
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_loadMediaNative(
    mut env: JNIEnv,
    obj: JObject,
    path: jstring,
) {
    unsafe {
        let j_string = JString::from_raw(path);
        let path_str: String = env
            .get_string(&j_string)
            .expect("Couldn't get java string")
            .into();
        debug!("Load path: {}", &path_str);

        if let Ok(file) = File::open(&path_str) {
            let reader = BufReader::new(file);

            let mss = MediaSourceStream::new(
                Box::new(File::open(&path_str).unwrap()),
                Default::default(),
            ); // TODO better file handling
            let hint = Hint::new();
            let probed = get_probe()
                .format(&hint, mss, &Default::default(), &Default::default())
                .unwrap();
            let format = probed.format;

            let track = format.default_track().unwrap();
            let params = &track.codec_params;

            let duration_seconds;
            if let (Some(sample_rate), Some(n_frames)) = (params.sample_rate, params.n_frames) {
                duration_seconds = (n_frames as f64 / sample_rate as f64);
            } else {
                duration_seconds = 0.0;
            }

            let source = Decoder::new(reader).unwrap().buffered();
            with_bridge(&mut env, obj, |_env, bridge| {
                bridge.setSource(source, duration_seconds);
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
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_unloadMediaNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |_env, bridge| {
        bridge.clearSource();
        trace!("Unload media");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_isMediaLoadedNative(
    mut env: JNIEnv,
    obj: JObject,
) -> jboolean {
    with_bridge(&mut env, obj, |_env, bridge| {
        trace!("Unload media");
        return bridge.source.is_some() as jboolean;
    })
    .unwrap()
}

fn with_bridge<T>(
    env: &mut JNIEnv,
    this: JObject,
    f: impl FnOnce(&mut JNIEnv, &mut RustBridge) -> T,
) -> Option<T> {
    let ptr = env
        .get_field(this, "nativePointer", "J")
        .unwrap()
        .j()
        .unwrap();
    if ptr == 0 {
        env.throw_new("java/lang/IllegalStateException", "Bridge not initialized")
            .unwrap();
        None
    } else {
        let bridge: &mut RustBridge = unsafe { &mut *(ptr as *mut RustBridge) };
        Some(f(env, bridge))
    }
}
