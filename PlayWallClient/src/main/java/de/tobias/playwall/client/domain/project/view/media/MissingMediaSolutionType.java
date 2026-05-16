package de.tobias.playwall.client.domain.project.view.media;

/**
 * Defines how a missing media entry should be resolved when the corresponding view is closed and the changes submitted.
 */
public enum MissingMediaSolutionType
{
	/**
	 * Do nothing (keep the media file path that leads to the loading error)
	 */
	NONE,
	/**
	 * Replace the media file path with the new selected path.
	 */
	REPLACE,
	/**
	 * Delete the pad content.
	 */
	DELETE;
}
