package de.tobias.playwall.nativeaudio.audio.mac;

public final class AVAudioBridge
{
	private AVAudioBridge()
	{
	}

	public static native AVAudioDevice[] getAudioDevices();

	public static native String getDeviceIdForDeviceName(String deviceName);
}
