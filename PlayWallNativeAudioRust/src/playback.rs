use crate::hybrid_loop_source::HybridLoopSource;
use crate::{with_audio_handler, AudioStreamHandler};
use jni::objects::JObject;
use jni::sys::{jboolean, jdouble, jlong};
use jni::JNIEnv;
use rodio::cpal::traits::HostTrait;
use rodio::{DeviceTrait, OutputStreamBuilder, Sink};
use tracing::debug;
use tracing::trace;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_playNative(
    mut e: JNIEnv,
    obj: JObject,
) {
    let global_obj = e.new_global_ref(&obj).expect("Failed to create global ref for EOF");
    let global_obj_progress = e.new_global_ref(&obj).expect("Failed to create global ref for progress");
    
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

            let looping_ptr = &audio_handler.looping as *const bool;

            let eof_callback = move || {
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
            };

            let progress_callback = move |seconds: f32| {
                if let Some(vm) = crate::JVM.read().unwrap().as_ref() {
                    if let Ok(mut env_local) = vm.attach_current_thread() {
                        let _ = env_local.call_method(
                            &global_obj_progress,
                            "onProgress",
                            "(D)V",
                            &[jni::objects::JValue::Double(seconds as f64)],
                        );
                    }
                }
            };

            let source = HybridLoopSource::new(path.clone(), looping_ptr, eof_callback, progress_callback);
            sink.set_volume(audio_handler.volume);
            sink.append(source);
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
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setLoopingNative(
    mut env: JNIEnv,
    obj: JObject,
    looping: jboolean,
) {
    with_audio_handler(&mut env, obj, |_env, audio_handler| {
        audio_handler.looping = looping != 0;
    });
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
