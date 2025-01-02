package de.tobias.playwall.server.common.pad.content;


import de.tobias.playwall.server.common.model.project.Pad;

public abstract class PadContent
{
	private Pad pad;

	public PadContent(Pad pad)
	{
		this.pad = pad;
	}

	public Pad getPad()
	{
		return pad;
	}

	public abstract String getType();

	/**
	 * Start playing the media of the pad
	 *
	 * @param withFadeIn a fade that indicates whether the content gets fade in or not
	 */
	public abstract void play(boolean withFadeIn);

	public abstract boolean stop();

	public abstract boolean isPadLoaded();

	public boolean isPadLoading()
	{
		return false;
	}

	/**
	 * Load media files.
	 */
	public abstract void loadMedia();

	/**
	 * Load media file.
	 *
	 * @param mediaPath specify media path
	 */
	public abstract void loadMedia(String mediaPath);

	/**
	 * Unload media files.
	 */
	public abstract void unloadMedia();

	/**
	 * Unload media file.
	 *
	 * @param mediaPath specify media path
	 */
	public abstract void unloadMedia(String mediaPath);

	public void reorderMedia()
	{
	}

	public abstract void updateVolume();
}