use crate::{AudioStreamHandler, with_bridge};
use jni::JNIEnv;
use jni::objects::JObject;
use jni::sys::{jboolean, jdouble, jlong};
use rodio::source::Buffered;
use rodio::{Decoder, OutputStreamBuilder, Sink, Source};
use std::fs::File;
use std::io::BufReader;
use tracing::trace;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_playNative(
    mut env: JNIEnv,
    obj: JObject,
    looping: jboolean,
) {
    with_bridge(&mut env, obj, |env, bridge| {
        if bridge.source.is_none() {
            env.throw_new("java/lang/IllegalStateException", "No media loaded")
                .unwrap();
            return;
        }

        if bridge.audio_stream_handler.is_none() {
            let stream_handler = OutputStreamBuilder::from_default_device()
                .unwrap()
                .open_stream()
                .unwrap();
            let sink = Sink::connect_new(stream_handler.mixer());
            bridge.setAudioHandlerStream(AudioStreamHandler {
                stream_handler,
                sink,
            });
            trace!("Init default output stream and sink");
        }

        let sink = &bridge.audio_stream_handler.as_ref().unwrap().sink;
        if sink.empty() {
            let shared_source: Buffered<Decoder<BufReader<File>>> = bridge.source.clone().unwrap();
            if looping == 1 {
                sink.append(shared_source.repeat_infinite());
            } else {
                sink.append(shared_source);
            }
            sink.play();
            trace!("Play (from existing audio handler)");
        } else {
            sink.play();
            trace!("Play (from existing audio handler, already playing)");
        }
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_pauseNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |_env, bridge| {
        bridge.audio_stream_handler.as_ref().unwrap().sink.pause();
        trace!("Pause");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_stopNative(
    mut env: JNIEnv,
    obj: JObject,
) {
    with_bridge(&mut env, obj, |_env, bridge| {
        bridge.audio_stream_handler.as_ref().unwrap().sink.stop();
        trace!("Stop");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_getDurationNative(
    mut env: JNIEnv,
    obj: JObject,
) -> jlong {
    with_bridge(&mut env, obj, |env, bridge| {
        if bridge.source.is_none() {
            env.throw_new("java/lang/IllegalStateException", "No media loaded")
                .unwrap();
            return 0;
        }
        return bridge.duration.unwrap().round() as jlong;
    })
    .unwrap()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setVolumeNative(
    mut env: JNIEnv,
    obj: JObject,
    volume: jdouble,
) {
    with_bridge(&mut env, obj, |_env, bridge| {
        bridge
            .audio_stream_handler
            .as_ref()
            .unwrap()
            .sink
            .set_volume(volume as f32);
        trace!("Set volume to {}", volume);
    });
}
