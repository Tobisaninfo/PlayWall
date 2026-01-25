package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadIdRequest;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.io.IOException;

abstract class PadPlaybackHandler<T extends RequestMessage & PadIdRequest> implements OneTimeActionRequestHandler<T>
{
	protected final ProjectController projectController;
	protected final MessageSource messageSource;

	PadPlaybackHandler(ProjectController projectController, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.messageSource = messageSource;
	}

	@Override
	public void handleRequest(T requestMessage) throws IOException, PlayWallServerException
	{
		final PadController controller = projectController.getPadController(requestMessage.getPadId());
		if(controller == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		handlePlayback(controller);
	}

	abstract void handlePlayback(PadController controller);
}
