package de.tobias.playwall.nativeaudio.audio.mac.delegate;

import de.tobias.playwall.nativeaudio.audio.mac.AVAudioPlayerBridge;

public interface NativeAudioDelegate
{
	void onFinish(AVAudioPlayerBridge bridge);

	void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right);

	void onPositionChanged(AVAudioPlayerBridge bridge, double position);
}
