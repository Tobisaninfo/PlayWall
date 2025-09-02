use std::fs::File;
use std::io::BufReader;
use jni::JNIEnv;
use jni::objects::JObject;
use jni::sys::jboolean;
use rodio::{Decoder, OutputStreamBuilder, Sink, Source};
use rodio::source::Buffered;
use tracing::trace;
use crate::{with_bridge, AudioStreamHandler};

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_playNative(
    mut env: JNIEnv,
    obj: JObject,
    looping: jboolean,
) {
    with_bridge(&mut env, obj, |bridge| {
        if bridge.audio_stream_handler.is_none() {
            let stream_handler = OutputStreamBuilder::from_default_device()
                .unwrap()
                .open_stream()
                .unwrap();
            let sink = Sink::connect_new(stream_handler.mixer());

            let shared_source: Buffered<Decoder<BufReader<File>>> = bridge.source.clone().unwrap();
            if looping == 1{
                sink.append(shared_source.repeat_infinite());
            } else {
                sink.append(shared_source);
            }
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