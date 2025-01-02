package de.tobias.playwall.server.common.audio;

public interface Peakable
{
	enum Channel
	{
		LEFT,
		RIGHT
	}

	Double audioLevel(Channel channel);

	double getAudioLevel(Channel channel);
}
