package de.tobias.playwall.nativeaudio.audio.rust;

import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.Soundcardable;
import de.tobias.playwall.server.common.project.PadController;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class NativeAudioRustHandler extends AudioHandler implements Soundcardable
{
	@SuppressWarnings("unused")
	private long nativePointer;

	public NativeAudioRustHandler(PadController padController)
	{
		super(padController);
		createNativeInstance();
		log.trace("Created new NativeAudioRustHandler with handle {}", nativePointer);
	}

	public static void initSystem(RustLogLevel logLevel) {
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
	public Duration getPosition()
	{
		return null;
	}

	@Override
	public Duration getDuration()
	{
		return null;
	}

	@Override
	public void setVolume(double volume)
	{

	}

	@Override
	public boolean isMediaLoaded()
	{
		return false;
	}

	@Override
	public void loadMedia(Path... paths)
	{
		if (paths.length != 1) {
			throw new IllegalArgumentException("Only one path is supported");
		}
		loadMediaNative(paths[0].toString());
	}

	private native void loadMediaNative(String path);

	@Override
	public void unloadMedia()
	{

	}

	@Override
	public void setOutputDevice(String name)
	{

	}
}
