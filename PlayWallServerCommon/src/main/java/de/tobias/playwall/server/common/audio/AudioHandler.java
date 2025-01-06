package de.tobias.playwall.server.common.audio;


import de.tobias.playwall.server.common.project.PadController;

import java.nio.file.Path;
import java.time.Duration;

public abstract class AudioHandler
{
	private PadController padController;

	public AudioHandler(PadController padController)
	{
		this.padController = padController;
	}

	public PadController getController()
	{
		return padController;
	}

	/**
	 * Start the audio stream
	 */
	public abstract void play();

	/**
	 * Pause the audio stream.
	 */
	public abstract void pause();

	/**
	 * Stop the audio stream.
	 */
	public abstract void stop();

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
	 * Set the current volume between 0 and 1.
	 *
	 * @param volume new volume
	 */
	public abstract void setVolume(double volume);

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
	 */
	public abstract void loadMedia(Path... paths);

	/**
	 * Unload Media to cleanup resources.
	 */
	public abstract void unloadMedia();
}
