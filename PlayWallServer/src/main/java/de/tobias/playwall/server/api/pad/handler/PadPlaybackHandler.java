package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadIdRequest;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

abstract class PadPlaybackHandler<T extends RequestMessage & PadIdRequest> implements RequestHandler<T>
{
	protected final ProjectController projectController;
	protected final MessageSource messageSource;

	PadPlaybackHandler(ProjectController projectController, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(T requestMessage) throws IOException, PlayWallServerException
	{
		final PadController controller = projectController.getPadController(requestMessage.getPadId());
		if(controller == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		handlePlayback(controller);

		return Optional.empty();
	}

	abstract void handlePlayback(PadController controller);
}
