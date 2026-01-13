package de.tobias.playwall.server.common.audio;

public interface AudioHandlerFactory
{
	AudioHandler createAudioHandler(Runnable eofCallback);
}
