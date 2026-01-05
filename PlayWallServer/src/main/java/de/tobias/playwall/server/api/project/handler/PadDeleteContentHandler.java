package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadDeleteContentRequest;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PadMapper;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PadDeleteContentRequest.class)
public class PadDeleteContentHandler implements RequestHandler<PadDeleteContentRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final MessageSource messageSource;

	public PadDeleteContentHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(PadDeleteContentRequest requestMessage) throws IOException, PlayWallServerException
	{
		final PadController oldController = projectController.getPadController(requestMessage.getPadId());
		if(oldController != null)
		{
			oldController.stop();
			oldController.unload();
		}

		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		pad.setContent(null);
		pad.setName(null);
		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));

		return Optional.empty();
	}
}
