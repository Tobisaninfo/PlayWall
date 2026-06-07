package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Duration;

@Slf4j
public class RustAudioHandler extends AudioHandler
{
	@SuppressWarnings("unused")
	private long nativePointer;
	private final Runnable eofCallback;
	private Duration position = Duration.ZERO;

	public RustAudioHandler(Runnable eofCallback)
	{
		this.eofCallback = eofCallback;
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
	public void play() throws IOException
	{
		playNative();
	}

	private native void playNative() throws IOException;

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
		position = Duration.ZERO;
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
		return position;
	}

	@Override
	public Duration getDuration()
	{
		return Duration.ofMillis((long) (getDurationNative() * 1000));
	}

	private native double getDurationNative();

	@Override
	public double getVolume()
	{
		return getVolumeNative();
	}

	private native double getVolumeNative();

	@Override
	public void setVolume(double volume)
	{
		if(volume < VolumeHelper.MIN_VOLUME || volume > VolumeHelper.MAX_VOLUME)
		{
			throw new IllegalArgumentException(MessageFormat.format("Volume must be between {0} and {1}. Value of {2} is not allowed", VolumeHelper.MIN_VOLUME, VolumeHelper.MAX_VOLUME, volume));
		}

		setVolumeNative(VolumeHelper.convertVolumeToLogarithmic(volume));
	}

	private native void setVolumeNative(double volume);

	@Override
	public boolean isMediaLoaded()
	{
		return isMediaLoadedNative();
	}

	private native boolean isMediaLoadedNative();

	@Override
	public void loadMedia(Path paths) throws IOException
	{
		loadMediaNative(paths.toString());
	}

	private native void loadMediaNative(String path) throws IOException;

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

	@Override
	public void seekTo(Duration position)
	{
		if(position.isNegative())
		{
			throw new IllegalArgumentException("Seek position must not be negative");
		}
		if(seekToPositionNative(position.toNanos() / 1_000_000_000.0))
		{
			this.position = position;
		}
	}

	private native boolean seekToPositionNative(double seconds);

	@Override
	public void setStartPosition(Duration position)
	{
		if(position.isNegative())
		{
			throw new IllegalArgumentException("Start position must not be negative");
		}
		setStartPositionNative(position.toNanos() / 1_000_000_000.0);
	}

	private native void setStartPositionNative(double seconds);

	@Override
	public void setEndPosition(Duration position)
	{
		if(position.isNegative())
		{
			throw new IllegalArgumentException("End position must not be negative");
		}
		setEndPositionNative(position.toNanos() / 1_000_000_000.0);
	}

	private native void setEndPositionNative(double seconds);

	@Override
	public void clearEndPosition()
	{
		clearEndPositionNative();
	}

	private native void clearEndPositionNative();

	@Override
	public void setPlaybackSpeed(double speed)
	{
		if(speed < 0.25 || speed > 4.0)
		{
			throw new IllegalArgumentException("Playback speed must be between 0.25 and 4.0. Current value is " + speed);
		}
		setPlaybackSpeedNative(speed);
	}

	private native void setPlaybackSpeedNative(double speed);

	// Callback from rust code
	@SuppressWarnings("unused")
	void onEof()
	{
		eofCallback.run();
		position = Duration.ZERO;
	}

	@SuppressWarnings("unused")
	void onError(Throwable exception)
	{
		log.error("Playback error during loop", exception);
		eofCallback.run();
		position = Duration.ZERO;
	}

	@SuppressWarnings("unused")
	void onProgress(double seconds)
	{
		position = Duration.ofMillis((long) (seconds * 1000));
	}
}
