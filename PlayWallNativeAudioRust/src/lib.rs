#![allow(non_snake_case)]

use jni::objects::{GlobalRef, JClass, JObject, JString, JValue};
use jni::sys::{jlong, jstring};
use jni::{JNIEnv, JavaVM};
use rodio::source::Buffered;
use rodio::{Decoder, OutputStream, OutputStreamBuilder, Sink, Source};
use std::fs::File;
use std::io::BufReader;
use std::str::FromStr;
use tracing::{debug, error, info, trace, warn};
use tracing_subscriber;

struct RustBridge {
    java_obj: GlobalRef,
    jvm: JavaVM,
    source: Option<Buffered<Decoder<BufReader<File>>>>,
    audio_stream_handler: Option<AudioStreamHandler>,
}

impl RustBridge {
    fn new(java_obj: GlobalRef, jvm: JavaVM) -> Self {
        Self {
            java_obj,
            jvm,
            source: None,
            audio_stream_handler: None,
        }
    }

    fn setSource(&mut self, source: Buffered<Decoder<BufReader<File>>>) {
        self.source = Some(source);
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

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_createNativeInstance(
    mut env: JNIEnv,
    this: JObject,
) {
    let global_ref = env.new_global_ref(&this).unwrap();
    let jvm = env.get_java_vm().unwrap();

    let bridge = Box::new(RustBridge::new(global_ref, jvm));
    let ptr = Box::into_raw(bridge) as jlong;

    env.set_field(&this, "nativePointer", "J", JValue::Long(ptr))
        .unwrap();
    trace!("Created bridge with ptr: {}", ptr);
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_destroy(
    mut env: JNIEnv,
    this: JObject,
) {
    let ptr = env
        .get_field(&this, "nativePointer", "J")
        .unwrap()
        .j()
        .unwrap();

    if ptr != 0 {
        unsafe {
            drop(Box::from_raw(ptr as *mut RustBridge));
        }
        env.set_field(&this, "nativePointer", "J", JValue::Long(0))
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
        debug!("Load path: {}", path_str);

        let file = File::open(path_str).unwrap(); // oder "sound.mp3", je nach Format
        let reader = BufReader::new(file);

        let source = Decoder::new(reader).unwrap().buffered();
        with_bridge(&mut env, obj, |bridge| {
            bridge.setSource(source);
            trace!("Loaded media");
        });
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_playNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |bridge| {
        if bridge.audio_stream_handler.is_none() {
            let stream_handler = OutputStreamBuilder::from_default_device()
                .unwrap()
                .open_stream()
                .unwrap();
            let sink = Sink::connect_new(stream_handler.mixer());

            let shared_source: Buffered<Decoder<BufReader<File>>> = bridge.source.clone().unwrap();
            sink.append(shared_source);
            bridge.setAudioHandlerStream(AudioStreamHandler {
                stream_handler,
                sink,
            });
            trace!("Play (from new audio handler)");
        } else {
            let sink = &bridge.audio_stream_handler.as_ref().unwrap().sink;
            if sink.empty() {
                let shared_source: Buffered<Decoder<BufReader<File>>> =
                    bridge.source.clone().unwrap();
                sink.append(shared_source);
                trace!("Play (from existing audio handler)");
            } else {
                sink.play();
                trace!("Play (from existing audio handler, already playing)");
            }
        }
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_pauseNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |bridge| {
        bridge.audio_stream_handler.as_ref().unwrap().sink.pause();
        trace!("Pause");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_stopNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |bridge| {
        bridge.audio_stream_handler.as_ref().unwrap().sink.stop();
        trace!("Stop");
    });
}

fn with_bridge<T>(env: &mut JNIEnv, this: JObject, f: impl FnOnce(&mut RustBridge) -> T) -> T {
    let ptr = env
        .get_field(this, "nativePointer", "J")
        .unwrap()
        .j()
        .unwrap();
    assert_ne!(ptr, 0, "Bridge not initialized");
    let bridge: &mut RustBridge = unsafe { &mut *(ptr as *mut RustBridge) };
    f(bridge)
}
