package de.tobias.playwall.server.project;

import java.time.Duration;

@FunctionalInterface
public interface PlaybackPositionListener
{
	void onPositionUpdate(Duration position, Duration duration);
}
