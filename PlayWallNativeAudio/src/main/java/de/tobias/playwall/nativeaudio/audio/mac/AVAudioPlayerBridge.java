package de.tobias.playwall.nativeaudio.audio.mac;

public class AVAudioPlayerBridge {

	private long nativePointer;

	@SuppressWarnings("java:S1144")
	private long getNativePointer() {
		return nativePointer;
	}

	@SuppressWarnings("java:S1144")
	private void setNativePointer(long nativePointer) {
		this.nativePointer = nativePointer;
	}


	static {
		initialize();
	}

	private static native void initialize();

	public AVAudioPlayerBridge() {
		init();
	}

	private native void init();

	public native void play();

	public native boolean isPlaying();

	public native void pause();

	public native void stop();

	public native void seek(double duration);

	public native void setLoop(boolean loop);

	public native double getVolume();

	public native void setVolume(double volume);

	public native boolean load(String path);

	public native void dispose();

	public native double getDuration();

	public native double getPosition();

	public native void setRate(double rate);

	public static native AudioDevice[] getAudioDevices();

	public native void setCurrentAudioDevice(String id);

	/*
	Delegate methods
	 */

	public void onPeakMeter(float left, float right) {
		if (delegate != null) {
			delegate.onPeakMeter(this, left, right);
		}
	}

	public void onPositionChanged(double position) {
		if (delegate != null) {
			delegate.onPositionChanged(this, position);
		}
	}

	public void onFinish() {
		if (delegate != null) {
			delegate.onFinish(this);
		}
	}

	private NativeAudioDelegate delegate;

	public void setDelegate(NativeAudioDelegate delegate) {
		this.delegate = delegate;
	}

	public interface NativeAudioDelegate {
		void onFinish(AVAudioPlayerBridge bridge);

		void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right);

		void onPositionChanged(AVAudioPlayerBridge bridge, double position);
	}
}
