use crate::{with_audio_handler, AudioStreamHandler};
use jni::errors::ThrowRuntimeExAndDefault;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::signature::{MethodSignature, RuntimeMethodSignature};
use jni::strings::JNIString;
use jni::sys::{jboolean, jint, jobjectArray, jsize};
use jni::EnvUnowned;
use rodio::cpal::traits::HostTrait;
use rodio::{DeviceSinkBuilder, DeviceTrait, Player};
use tracing::trace;

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
            ))
            .expect("AudioDevice class not found");
        let devices: Vec<_> = host.output_devices().unwrap().collect();
        let default_device = host.default_output_device().unwrap();

        let result = env
            .new_object_array(devices.len() as jsize, &audio_device_class, JObject::null())
            .unwrap();
        for (index, device) in devices.into_iter().enumerate() {
            let param = &RuntimeMethodSignature::from_str("(Ljava/lang/String;IIZ)V").unwrap();
            let sig = MethodSignature::from(param);

            let deviceName: JString = env.new_string(device.description().unwrap().name()).unwrap();
            let stream_config = device.default_output_config().unwrap().config();
            let channels = stream_config.channels as jint;
            let sample_rate = stream_config.sample_rate as jint;
            let default_device =
                (device.description().unwrap().name() == default_device.description().unwrap().name()) as jboolean;

            let java_audio_device = env
                .new_object(
                    &audio_device_class,
                    sig,
                    &[
                        JValue::Object(&deviceName),
                        JValue::Int(channels),
                        JValue::Int(sample_rate),
                        JValue::Bool(default_device),
                    ],
                )
                .unwrap();

            result.set_element(env, index, java_audio_device).expect("Failed to set element");
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
) {
    let _ = env.with_env(|mut env| {
        let device_name_str: String = device_name.to_string();
        let host = rodio::cpal::default_host();
        let device: Option<_> = host
            .output_devices()
            .unwrap()
            .find(|device| device.description().unwrap().name() == device_name_str);

        if device.is_none() {
            env.throw_new(
                JNIString::new("java/lang/IllegalArgumentException"),
                JNIString::new(format!(
                    "No output device found with name \"{}\"",
                    device_name_str
                )),
            )
                .unwrap();
            return Ok::<(), jni::errors::Error>(());
        }

        with_audio_handler(&mut env, object, |_, audio_handler| {
            let stream_handler = DeviceSinkBuilder::from_device(device.unwrap())
                .unwrap()
                .open_stream()
                .unwrap();
            let sink = Player::connect_new(stream_handler.mixer());
            audio_handler.setAudioHandlerStream(AudioStreamHandler {
                stream_handler,
                sink,
            });
            audio_handler.device_name = Some(device_name_str.clone());
            trace!("Init output stream and sink for device {}", device_name_str);
        });
        Ok::<(), jni::errors::Error>(())
    });
}
