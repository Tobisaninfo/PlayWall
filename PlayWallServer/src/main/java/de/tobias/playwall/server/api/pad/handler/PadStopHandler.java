package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.project.PadStopRequest;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(PadStopRequest.class)
class PadStopHandler extends PadPlaybackHandler<PadStopRequest>
{
	public PadStopHandler(ProjectController projectController, MessageSource messageSource)
	{
		super(projectController, messageSource);
	}

	@Override
	void handlePlayback(PadController controller)
	{
		controller.stop();
	}
}
