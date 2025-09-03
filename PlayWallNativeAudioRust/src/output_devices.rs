use jni::objects::{JClass, JObject, JString, JValue};
use jni::sys::{jboolean, jint, jobjectArray, jsize};
use jni::JNIEnv;
use rodio::cpal::traits::HostTrait;
use rodio::DeviceTrait;

#[unsafe(no_mangle)]
pub extern "system" fn Java_de_tobias_playwall_nativeaudio_audio_rust_NativeAudioRustHandler_getOutputDevices(
    mut env: JNIEnv,
    _class: JClass,
) -> jobjectArray {
    let host = rodio::cpal::default_host();

    let audio_device_class = env.find_class("de/tobias/playwall/nativeaudio/audio/rust/AudioDevice")
        .expect("AudioDevice class not found");
    let devices: Vec<_> = host.output_devices().unwrap().collect();
    let default_device = host.default_output_device().unwrap();

    let result = env.new_object_array(devices.len() as jsize, &audio_device_class, JObject::null()).unwrap();
    for (index, device) in devices.into_iter().enumerate() {
        let ctor_sig = "(Ljava/lang/String;IIZ)V";

        let deviceName: JString = env.new_string(device.name().unwrap()).unwrap();
        let stream_config = device.default_output_config().unwrap().config();
        let channels = stream_config.channels as jint;
        let sample_rate = stream_config.sample_rate.0 as jint;
        let default_device = (device.name().unwrap() == default_device.name().unwrap()) as jboolean;

        let java_audio_device = env.new_object(
            &audio_device_class,
            ctor_sig,
            &[JValue::Object(&deviceName), JValue::Int(channels), JValue::Int(sample_rate), JValue::Bool(default_device)],
        ).unwrap();
        env.set_object_array_element(&result, index as jsize, java_audio_device).unwrap();
    }
    result.into_raw() as jobjectArray
}
