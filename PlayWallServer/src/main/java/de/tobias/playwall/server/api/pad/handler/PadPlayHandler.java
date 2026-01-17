package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.project.PadPlayRequest;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(PadPlayRequest.class)
public class PadPlayHandler extends PadPlaybackHandler<PadPlayRequest>
{
	public PadPlayHandler(ProjectController projectController, MessageSource messageSource)
	{
		super(projectController, messageSource);
	}

	@Override
	void handlePlayback(PadController controller)
	{
		controller.play(true);
	}
}
