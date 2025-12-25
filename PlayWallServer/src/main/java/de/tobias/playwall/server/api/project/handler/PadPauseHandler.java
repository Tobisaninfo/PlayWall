package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadPauseRequest;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandlerTyped;

@RequestHandlerTyped(PadPauseRequest.class)
public class PadPauseHandler extends PadPlaybackHandler<PadPauseRequest>
{
	@Override
	void handlePlayback(PadController controller)
	{
		controller.pause();
	}
}
