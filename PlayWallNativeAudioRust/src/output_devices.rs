use crate::{with_audio_handler};
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::sys::{jboolean, jint, jobjectArray, jsize};
use jni::EnvUnowned;
use rodio::cpal::traits::HostTrait;
use rodio::{DeviceTrait};
use std::sync::{Mutex, OnceLock};
use tracing::trace;

static DEVICE_CACHE: OnceLock<Mutex<Vec<String>>> = OnceLock::new();

fn device_cache() -> &'static Mutex<Vec<String>> {
    DEVICE_CACHE.get_or_init(|| Mutex::new(Vec::new()))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_getOutputDevices(
    mut env: EnvUnowned,
    _class: JClass,
) -> jobjectArray {
    env.with_env(|env| -> jni::errors::Result<jobjectArray> {
        let host = rodio::cpal::default_host();

        let audio_device_class = env
            .find_class(JNIString::new(
                "de/tobias/playwall/nativeaudio/audio/rust/AudioDevice",
            ))?;

        let raw_devices: Vec<_> = match host.output_devices() {
            Ok(devices) => devices.collect(),
            Err(err) => {
                tracing::warn!("Failed to enumerate output devices: {}", err);
                Vec::new()
            }
        };

        // Some ALSA/PipeWire device nodes fail to report a name or default config
        // (e.g. stale/disconnected cards). Skip those instead of aborting the whole
        // enumeration, since a single bad device previously caused a Rust panic here.
        let devices: Vec<(String, rodio::cpal::SupportedStreamConfig)> = raw_devices
            .into_iter()
            .filter_map(|device| {
                let name = match device.description() {
                    Ok(desc) => desc.name().to_string(),
                    Err(err) => {
                        tracing::warn!("Skipping output device with unreadable name: {}", err);
                        return None;
                    }
                };
                match device.default_output_config() {
                    Ok(config) => Some((name, config)),
                    Err(err) => {
                        tracing::warn!("Skipping output device \"{}\" without a usable default config: {}", name, err);
                        None
                    }
                }
            })
            .collect();

        let default_device_name: Option<String> = host
            .default_output_device()
            .and_then(|d| d.description().ok())
            .map(|desc| desc.name().to_string());

        *device_cache().lock().unwrap() = devices.iter().map(|(name, _)| name.clone()).collect();

        let result = env.new_object_array(devices.len() as jsize, &audio_device_class, JObject::null())?;
        for (index, (name, config)) in devices.into_iter().enumerate() {
            let param = &RuntimeMethodSignature::from_str("(Ljava/lang/String;IIZ)V").unwrap();
            let sig = MethodSignature::from(param);

            let device_name: JString = env.new_string(&name)?;
            let stream_config = config.config();
            let channels = stream_config.channels as jint;
            let sample_rate = stream_config.sample_rate as jint;
            let is_default = (default_device_name.as_deref() == Some(name.as_str())) as jboolean;

            let java_audio_device = env.new_object(
                &audio_device_class,
                sig,
                &[
                    JValue::Object(&device_name),
                    JValue::Int(channels),
                    JValue::Int(sample_rate),
                    JValue::Bool(is_default),
                ],
            )?;

            result.set_element(env, index, java_audio_device)?;
        }
        Ok(result.into_raw() as jobjectArray)
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_RustAudioHandler_setOutputDeviceNative(
    mut env: EnvUnowned,
    object: JObject,
    device_name: JString,
    should_use_system_default_as_fallback: jboolean
) {
    env.with_env(|mut env| -> jni::errors::Result<()> {
        if device_name.is_null() {
            with_audio_handler(&mut env, object, |_, audio_handler| {
                audio_handler.clearAudioHandlerStream();
                audio_handler.device_name = None;
                trace!("Cleared output device, will use default");
            });
            return Ok(());
        }

        let device_name_str: String = device_name.to_string();
        let cache = device_cache().lock().unwrap();
        let device_exists = if cache.is_empty() {
            let host = rodio::cpal::default_host();
            match host.output_devices() {
                Ok(mut devices) => devices.any(|d| {
                    d.description()
                        .map(|desc| desc.name() == device_name_str)
                        .unwrap_or(false)
                }),
                Err(err) => {
                    tracing::warn!("Failed to enumerate output devices: {}", err);
                    false
                }
            }
        } else {
            cache.contains(&device_name_str)
        };
        drop(cache);

        if !device_exists && !should_use_system_default_as_fallback {
            env.throw_new(
                JNIString::new("java/lang/IllegalArgumentException"),
                JNIString::new(format!(
                    "No output device found with name \"{}\"",
                    device_name_str
                )),
            )?;
            return Ok(());
        }

        with_audio_handler(&mut env, object, |_, audio_handler| {
            audio_handler.clearAudioHandlerStream();
            if device_exists {
                audio_handler.device_name = Some(device_name_str.clone());
            } else {
                audio_handler.device_name = None;
            }
            trace!("Init output stream and sink for device {}", device_name_str);
        });
        Ok(())
    })
        .resolve::<ThrowRuntimeExAndDefault>()
}
