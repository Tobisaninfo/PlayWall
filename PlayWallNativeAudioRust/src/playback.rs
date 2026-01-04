use crate::{with_audio_handler, AudioStreamHandler};
use jni::objects::JObject;
use jni::sys::{jboolean, jdouble, jlong};
use jni::JNIEnv;
use rodio::cpal::traits::HostTrait;
use rodio::{Decoder, DeviceTrait, OutputStreamBuilder, Sink};
use std::fs::File;
use std::io::BufReader;
use tracing::trace;
use tracing::debug;
use crate::looping_source::LoopingSource;
use crate::eof_callback_source::EofCallbackSource;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_playNative(
    mut e: JNIEnv,
    obj: JObject,
    looping: jboolean,
) {
    let obj_for_global = unsafe { JObject::from_raw(obj.as_raw()) };
    let global_obj = e.new_global_ref(obj_for_global).expect("...");
    let mut global_obj_opt = Some(global_obj);

    with_audio_handler(&mut e, obj, |env, audio_handler| {
        if audio_handler.media_path.is_none() {
            env.throw_new("java/lang/IllegalStateException", "No media loaded")
                .unwrap();
            return;
        }

        if audio_handler.audio_stream_handler.is_none() {
            let host = rodio::cpal::default_host();
            let device = if let Some(ref name) = audio_handler.device_name {
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
            audio_handler.setAudioHandlerStream(AudioStreamHandler {
                stream_handler,
                sink,
            });
            trace!("Init output stream and sink (recreated)");
        }

        let sink = &audio_handler.audio_stream_handler.as_ref().unwrap().sink;
        if sink.empty() {
            let path = audio_handler.media_path.as_ref().unwrap();

            if looping == 1 {
                let source = LoopingSource::new(path.clone());
                sink.set_volume(audio_handler.volume);
                sink.append(source);
            } else {
                let file = File::open(path).expect("Failed to open file");
                let reader = BufReader::new(file);
                let source = Decoder::new(reader).expect("Failed to create decoder");

                let eof_source = EofCallbackSource::new(source, move || {
                    if let Some(vm) = crate::JVM.read().unwrap().as_ref() {
                        if let Ok(mut env_local) = vm.attach_current_thread() {
                            trace!("Invoking Java onEof callback");
                            if let Some(obj_ref) = global_obj_opt.take() {
                                if let Err(e) = env_local.call_method(&obj_ref, "onEof", "()V", &[]) {
                                    debug!("Failed to call Java onEof: {:?}", e);
                                }
                            }
                        }
                    }
                });
                sink.set_volume(audio_handler.volume);
                sink.append(eof_source);
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
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        if audio_handler.audio_stream_handler.as_ref().is_some() {
            audio_handler.audio_stream_handler.as_ref().unwrap().sink.pause();
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
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        if let Some(handler) = audio_handler.audio_stream_handler.take() {
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
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        return if audio_handler.audio_stream_handler.as_ref().is_some() {
            let paused = audio_handler
                .audio_stream_handler
                .as_ref()
                .unwrap()
                .sink
                .is_paused();
            let is_empty = audio_handler.audio_stream_handler.as_ref().unwrap().sink.empty();
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
    with_audio_handler(&mut env, obj, |env, audio_handler| {
        if audio_handler.media_path.is_none() {
            env.throw_new("java/lang/IllegalStateException", "No media loaded")
                .unwrap();
            return 0;
        }
        return audio_handler.duration.unwrap().round() as jlong;
    })
    .unwrap()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setVolumeNative(
    mut env: JNIEnv,
    obj: JObject,
    volume: jdouble,
) {
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        audio_handler.volume = volume as f32;
        if audio_handler.audio_stream_handler.as_ref().is_some() {
            audio_handler
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
