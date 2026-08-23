package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadPauseRequest;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(PadPauseRequest.class)
class PadPauseHandler extends PadPlaybackHandler<PadPauseRequest>
{
	public PadPauseHandler(ProjectController projectController, MessageSource messageSource)
	{
		super(projectController, messageSource);
	}

	@Override
	void handlePlayback(PadController controller, PadPauseRequest requestMessage)
	{
		controller.pause();
	}
}
