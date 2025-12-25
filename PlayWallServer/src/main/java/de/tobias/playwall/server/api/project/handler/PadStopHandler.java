package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadStopRequest;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestHandlerTyped(PadStopRequest.class)
public class PadStopHandler extends PadPlaybackHandler<PadStopRequest>
{
	@Override
	void handlePlayback(PadController controller)
	{
		controller.stop();
	}
}
