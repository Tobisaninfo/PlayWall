package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadPlayRequest;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestHandlerTyped(PadPlayRequest.class)
public class PadPlayHandler extends PadPlaybackHandler<PadPlayRequest>
{
	@Override
	void handlePlayback(PadController controller)
	{
		controller.play(true);
	}
}
