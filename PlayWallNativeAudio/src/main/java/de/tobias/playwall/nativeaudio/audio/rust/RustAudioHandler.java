package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.project.PadController;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class RustAudioHandler extends AudioHandler
{
	@SuppressWarnings("unused")
	private long nativePointer;

	public RustAudioHandler(PadController padController)
	{
		super(padController);
		createNativeInstance();
		log.trace("Created new NativeAudioRustHandler with handle {}", nativePointer);
	}

	public static void initSystem(RustLogLevel logLevel)
	{
		initSystem(logLevel.getLevel());
	}

	private static native void initSystem(String logLevel);

	private native long createNativeInstance();

	@Override
	public void play()
	{
		playNative();
	}

	private native void playNative();

	@Override
	public void pause()
	{
		pauseNative();
	}

	private native void pauseNative();

	@Override
	public void stop()
	{
		stopNative();
	}

	private native void stopNative();

	@Override
	public boolean isPlaying()
	{
		return isPlayingNative();
	}

	private native boolean isPlayingNative();

	@Override
	public void setLooping(boolean looping)
	{
		setLoopingNative(looping);
	}

	private native void setLoopingNative(boolean looping);

	@Override
	public Duration getPosition()
	{
		return null;
	}

	@Override
	public Duration getDuration()
	{
		return Duration.ofSeconds(getDurationNative());
	}

	private native long getDurationNative();

	@Override
	public void setVolume(double volume)
	{
		if(volume < 0 || volume > 1)
		{
			throw new IllegalArgumentException("Volume must be between 0 and 1");
		}
		setVolumeNative(volume);
	}

	private native void setVolumeNative(double volume);

	@Override
	public boolean isMediaLoaded()
	{
		return isMediaLoadedNative();
	}

	private native boolean isMediaLoadedNative();

	@Override
	public void loadMedia(Path paths)
	{
		loadMediaNative(paths.toString());
	}

	private native void loadMediaNative(String path);

	@Override
	public void unloadMedia()
	{
		if(isMediaLoaded())
		{
			unloadMediaNative();
		}
	}

	private native void unloadMediaNative();

	@Override
	public void setOutputDevice(String name)
	{
		setOutputDeviceNative(name);
	}

	private native void setOutputDeviceNative(String name);

	public static native AudioDevice[] getOutputDevices();

	// Callback from rust code
	@SuppressWarnings("unused")
	void onEof()
	{
		getController().onEof();
	}

	@SuppressWarnings("unused")
	void onProgress(double seconds)
	{
		log.debug("Progress: {} seconds", seconds);
		// Hier kann der Fortschritt verarbeitet werden, z.B. an das UI weitergeben
	}
}
