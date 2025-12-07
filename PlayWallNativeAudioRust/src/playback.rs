use crate::{AudioStreamHandler, with_bridge};
use jni::JNIEnv;
use jni::objects::JObject;
use jni::sys::{jboolean, jdouble, jlong};
use rodio::source::Buffered;
use rodio::{Decoder, DeviceTrait, OutputStreamBuilder, Sink, Source};
use std::fs::File;
use std::io::BufReader;
use rodio::cpal::traits::HostTrait;
use tracing::trace;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_playNative(
    mut env: JNIEnv,
    obj: JObject,
    looping: jboolean,
) {
    with_bridge(&mut env, obj, |env, bridge| {
        if bridge.media_path.is_none() {
            env.throw_new("java/lang/IllegalStateException", "No media loaded")
                .unwrap();
            return;
        }

        if bridge.audio_stream_handler.is_none() {
            let host = rodio::cpal::default_host();
            let device = if let Some(ref name) = bridge.device_name {
                host.output_devices().unwrap().find(|d| d.name().unwrap_or_default() == *name)
            } else {
                None
            };

            let stream_builder = if let Some(d) = device {
                OutputStreamBuilder::from_device(d)
            } else {
                OutputStreamBuilder::from_default_device()
            };

            let stream_handler = stream_builder
                .unwrap()
                .open_stream()
                .unwrap();
            let sink = Sink::connect_new(stream_handler.mixer());
            bridge.setAudioHandlerStream(AudioStreamHandler {
                stream_handler,
                sink,
            });
            trace!("Init output stream and sink (recreated)");
        }

        let sink = &bridge.audio_stream_handler.as_ref().unwrap().sink;
        if sink.empty() {
            let path = bridge.media_path.as_ref().unwrap();
            let file = File::open(path).expect("Failed to open file");
            let reader = BufReader::new(file);
            let source = Decoder::new(reader).expect("Failed to create decoder");

            if looping == 1 {
                sink.append(source.buffered().repeat_infinite());
            } else {
                sink.append(source);
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
        if let Some(handler) = bridge.audio_stream_handler.take() {
            handler.sink.stop();
            // handler will be dropped from memory to free resources
        }
        trace!("Stop");
    });
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_getDurationNative(
    mut env: JNIEnv,
    obj: JObject,
) -> jlong {
    with_bridge(&mut env, obj, |env, bridge| {
        if bridge.media_path.is_none() {
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
