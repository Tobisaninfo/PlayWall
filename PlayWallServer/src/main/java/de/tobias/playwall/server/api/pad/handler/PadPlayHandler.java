package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadPlayRequest;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.io.UncheckedIOException;

@RequestHandlerTyped(PadPlayRequest.class)
class PadPlayHandler extends PadPlaybackHandler<PadPlayRequest>
{
	public PadPlayHandler(ProjectController projectController, MessageSource messageSource)
	{
		super(projectController, messageSource);
	}

	@Override
	void handlePlayback(PadController controller, PadPlayRequest requestMessage)
	{
		try
		{
			controller.play();
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
