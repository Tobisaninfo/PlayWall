package de.tobias.playwall.server.common.audio;

import de.tobias.playwall.server.common.project.PadController;

public interface AudioHandlerFactory
{
	AudioHandler createAudioHandler(PadController controller);
}
