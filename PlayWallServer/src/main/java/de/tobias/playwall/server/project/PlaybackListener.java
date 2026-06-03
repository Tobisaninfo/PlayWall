package de.tobias.playwall.server.project;

import java.time.Duration;

public interface PlaybackListener
{
	void onPositionUpdate(Duration position, Duration duration);

	void onPlay();

	void onEof();
}
