package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadPauseRequest;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(PadPauseRequest.class)
public class PadPauseHandler extends PadPlaybackHandler<PadPauseRequest>
{
	public PadPauseHandler(ProjectController projectController, MessageSource messageSource)
	{
		super(projectController, messageSource);
	}

	@Override
	void handlePlayback(PadController controller)
	{
		controller.pause();
	}
}
