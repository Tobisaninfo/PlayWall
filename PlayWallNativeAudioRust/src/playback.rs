use crate::hybrid_loop_source::HybridLoopSource;
use crate::{with_audio_handler, AudioStreamHandler};
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::JObject;
use jni::strings::JNIString;
use jni::sys::{jboolean, jdouble};
use jni::EnvUnowned;
use rodio::cpal::traits::HostTrait;
use rodio::{DeviceSinkBuilder, DeviceTrait, Player};
use std::io;
use std::time::Duration;
use tracing::trace;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_playNative(
    mut e: EnvUnowned,
    obj: JObject,
) {
    e.with_env(|mut e| -> jni::errors::Result<()> {
        let global_obj = e.new_global_ref(&obj)?;

        let result = with_audio_handler(&mut e, obj, |env, audio_handler| -> io::Result<()> {
            if audio_handler.media_path.is_none() {
                env.throw_new(
                    JNIString::new("java/lang/IllegalStateException"),
                    JNIString::new("No media loaded"),
                )
                    .ok();
                return Ok(());
            }

            if audio_handler.audio_stream_handler.is_none() {
                let host = rodio::cpal::default_host();
                let device = if let Some(ref name) = audio_handler.device_name {
                    host.output_devices()
                        .unwrap()
                        .find(|d| d.description().unwrap().name() == *name)
                } else {
                    None
                };

                let stream_builder = if let Some(d) = device {
                    DeviceSinkBuilder::from_device(d)
                } else {
                    DeviceSinkBuilder::from_default_device()
                };

                let mut stream_handler = stream_builder.unwrap().open_stream().unwrap();
                stream_handler.log_on_drop(false);
                let sink = Player::connect_new(stream_handler.mixer());
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

                let jvm_static: &'static jni::JavaVM = unsafe {
                    let jvm_lock = crate::JVM.read().unwrap();
                    let jvm_ref = jvm_lock.as_ref().expect("JVM not initialized");
                    &*(jvm_ref as *const jni::JavaVM)
                };

                let source = HybridLoopSource::new(path.clone(), looping_ptr, jvm_static, global_obj)
                    .map_err(|e| io::Error::new(e.kind(), format!("{}: {}", path, e)))?;

                sink.set_volume(audio_handler.volume);
                sink.append(source);
                sink.play();
                trace!("Play (from existing audio handler)");
            } else {
                sink.play();
                trace!("Play (from existing audio handler, already playing)");
            }
            Ok(())
        });

        if let Some(Err(io_err)) = result {
            let exception_class = if io_err.kind() == io::ErrorKind::NotFound {
                "java/io/FileNotFoundException"
            } else {
                "java/io/IOException"
            };
            e.throw_new(
                JNIString::new(exception_class),
                JNIString::new(io_err.to_string()),
            )?;
        }

        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_pauseNative(
    mut env: EnvUnowned,
    obj: JObject,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        with_audio_handler(&mut env, obj, |_env, audio_handler| {
            if audio_handler.audio_stream_handler.as_ref().is_some() {
                audio_handler
                    .audio_stream_handler
                    .as_ref()
                    .unwrap()
                    .sink
                    .pause();
                trace!("Pause");
            } else {
                trace!("No audio handler to pause, skipping");
            }
        });
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_stopNative(
    mut env: EnvUnowned,
    obj: JObject,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        with_audio_handler(&mut env, obj, |_env, audio_handler| {
            if let Some(handler) = audio_handler.audio_stream_handler.take() {
                handler.sink.stop();
                // handler will be dropped from memory to free resources
            }
            trace!("Stop");
        });
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_isPlayingNative(
    mut unowned_env: EnvUnowned,
    obj: JObject,
) -> jboolean {
    unowned_env
        .with_env(|mut env| -> jni::errors::Result<jboolean> {
            let playing = with_audio_handler(&mut env, obj, |_env, audio_handler| {
                return if audio_handler.audio_stream_handler.as_ref().is_some() {
                    let paused = audio_handler
                        .audio_stream_handler
                        .as_ref()
                        .unwrap()
                        .sink
                        .is_paused();
                    let is_empty = audio_handler
                        .audio_stream_handler
                        .as_ref()
                        .unwrap()
                        .sink
                        .empty();
                    (!paused && !is_empty) as jboolean
                } else {
                    false as jboolean
                };
            })
                .unwrap();
            Ok(playing as jboolean)
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setLoopingNative(
    mut env: EnvUnowned,
    obj: JObject,
    looping: jboolean,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        with_audio_handler(&mut env, obj, |_env, audio_handler| {
            audio_handler.looping = looping;
        });
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_getDurationNative(
    mut unowned_env: EnvUnowned,
    obj: JObject,
) -> jdouble {
    unowned_env
        .with_env(|mut env| -> jni::errors::Result<jdouble> {
            let duration = with_audio_handler(&mut env, obj, |env, audio_handler| {
                if audio_handler.media_path.is_none() {
                    env.throw_new(
                        JNIString::new("java/lang/IllegalStateException"),
                        JNIString::new("No media loaded"),
                    )
                        .ok();
                    return 0.0;
                }
                audio_handler.duration.unwrap() as jdouble
            })
                .unwrap();
            return Ok(duration as jdouble);
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_getVolumeNative(
    mut unowned_env: EnvUnowned,
    obj: JObject,
) -> jdouble {
    unowned_env
        .with_env(|mut env| -> jni::errors::Result<jdouble> {
            let duration = with_audio_handler(&mut env, obj, |_, audio_handler| {
                audio_handler.volume as jdouble
            })
                .unwrap();
            return Ok(duration as jdouble);
        })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setVolumeNative(
    mut env: EnvUnowned,
    obj: JObject,
    volume: jdouble,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
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
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_seekToPositionNative(
    mut env: EnvUnowned,
    obj: JObject,
    seconds: jdouble,
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        with_audio_handler(&mut env, obj, |env, audio_handler| {
            if let Some(ref handler) = audio_handler.audio_stream_handler {
                let pos = Duration::from_secs_f64(seconds);
                if let Err(e) = handler.sink.try_seek(pos) {
                    env.throw_new(
                        JNIString::new("java/lang/IllegalStateException"),
                        JNIString::new(format!("Seek failed: {}", e)),
                    ).ok();
                } else {
                    trace!("Seek to {}s", seconds);
                }
            } else {
                trace!("No audio handler to seek, skipping");
            }
        });
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

