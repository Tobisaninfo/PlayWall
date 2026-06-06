package de.tobias.playwall.server.common.audio;


import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public abstract class AudioHandler
{
	protected AudioHandler()
	{
	}

	/**
	 * Start the audio stream
	 *
	 * @throws IOException error on file loading, file is missing, format error
	 */
	public abstract void play() throws IOException;

	/**
	 * Pause the audio stream.
	 */
	public abstract void pause();

	/**
	 * Stop the audio stream.
	 */
	public abstract void stop();

	/**
	 * Return true if the current media is playing.
	 *
	 * @return true if playing
	 */
	public abstract boolean isPlaying();

	/**
	 * Set looping on or off.
	 *
	 * @param looping true to enable looping
	 */
	public abstract void setLooping(boolean looping);

	/**
	 * Get the current play position of the current player.
	 *
	 * @return current position
	 */
	public abstract Duration getPosition();

	/**
	 * Get the duration of the current player.
	 *
	 * @return duration
	 */
	public abstract Duration getDuration();

	/**
	 * Set the current volume between 0 and 1.25.
	 *
	 * @param volume new volume
	 */
	public abstract void setVolume(double volume);

	/**
	 * Get the current volume between 0 and 1.25.
	 *
	 * @return current volume
	 */
	public abstract double getVolume();

	/**
	 * Check if media is loaded.
	 *
	 * @return <code>true</code> Loaded
	 */
	public abstract boolean isMediaLoaded();

	/**
	 * prepare a set of media to be played.
	 *
	 * @param paths path to the audio files
	 * @throws IOException error on file loading, file is missing, format error
	 */
	public abstract void loadMedia(Path paths) throws IOException;

	/**
	 * Unload Media to cleanup resources.
	 */
	public abstract void unloadMedia();

	/**
	 * Sets the audio device.
	 *
	 * @param name audio device name
	 */
	public abstract void setOutputDevice(String name);

	/**
	 * Seek to the beginning of the current media without stopping playback.
	 * Has no effect if no stream is active.
	 */
	public void seekToStart()
	{
		seekTo(Duration.ZERO);
	}

	/**
	 * Seek to an arbitrary position within the current media without stopping playback.
	 * Has no effect if no stream is active.
	 *
	 * @param position target position
	 */
	public abstract void seekTo(Duration position);

	/**
	 * Set the position at which playback starts (and restarts on loop).
	 * Must be called before {@link #play()}; has no effect on an already running stream.
	 *
	 * @param position start position
	 */
	public abstract void setStartPosition(Duration position);

	/**
	 * Set the position at which playback stops (and loops back on loop).
	 * Must be called before {@link #play()}; has no effect on an already running stream.
	 *
	 * @param position end position
	 */
	public abstract void setEndPosition(Duration position);

	/**
	 * Remove the configured end position so playback continues to the end of the file.
	 * Must be called before {@link #play()}; has no effect on an already running stream.
	 */
	public abstract void clearEndPosition();

	/**
	 * Set the playback speed without changing the pitch (time-stretching).
	 * Takes effect immediately on a running stream.
	 *
	 * @param speed multiplier in the range [0.25, 4.0]; 1.0 = normal speed
	 */
	public abstract void setPlaybackSpeed(double speed);
}
