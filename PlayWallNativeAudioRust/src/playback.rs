use crate::{with_bridge, AudioStreamHandler};
use jni::objects::JObject;
use jni::sys::{jboolean, jdouble, jlong};
use jni::JNIEnv;
use rodio::cpal::traits::HostTrait;
use rodio::{Decoder, DeviceTrait, OutputStreamBuilder, Sink, Source};
use std::fs::File;
use std::io::BufReader;
use tracing::trace;

use crate::looping_source::LoopingSource;

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
                host.output_devices()
                    .unwrap()
                    .find(|d| d.name().unwrap_or_default() == *name)
            } else {
                None
            };

            let stream_builder = if let Some(d) = device {
                OutputStreamBuilder::from_device(d)
            } else {
                OutputStreamBuilder::from_default_device()
            };

            let stream_handler = stream_builder.unwrap().open_stream().unwrap();
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

            if looping == 1 {
                let source = LoopingSource::new(path.clone());
                sink.append(source);
            } else {
                let file = File::open(path).expect("Failed to open file");
                let reader = BufReader::new(file);
                let source = Decoder::new(reader).expect("Failed to create decoder");
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
        if (bridge.audio_stream_handler.as_ref().is_some()) {
            bridge.audio_stream_handler.as_ref().unwrap().sink.pause();
            trace!("Pause");
        } else {
            trace!("No audio handler to pause, skipping");
        }
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
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_isPlayingNative(
    mut env: JNIEnv,
    obj: JObject,
) -> jboolean {
    with_bridge(&mut env, obj, |_env, bridge| {
        return if (bridge.audio_stream_handler.as_ref().is_some()) {
            let paused = bridge
                .audio_stream_handler
                .as_ref()
                .unwrap()
                .sink
                .is_paused();
            let is_empty = bridge.audio_stream_handler.as_ref().unwrap().sink.empty();
            (!paused && !is_empty) as jboolean
        } else {
            false as jboolean
        };
    })
        .unwrap()
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
        if (bridge.audio_stream_handler.as_ref().is_some()) {
            bridge
                .audio_stream_handler
                .as_ref()
                .unwrap()
                .sink
                .set_volume(volume as f32);
            trace!("Set volume to {}", volume);
        } else {
            trace!("No audio handler to set volume, skipping");
        }
    });
}
