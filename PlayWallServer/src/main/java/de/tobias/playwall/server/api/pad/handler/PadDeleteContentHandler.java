package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.project.PadDeleteContentRequest;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.util.List;

@RequestHandlerTyped(PadDeleteContentRequest.class)
public class PadDeleteContentHandler extends UndoableRequestHandler<PadDeleteContentRequest>
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
	public void handleUndoableRequest(PadDeleteContentRequest requestMessage) throws PlayWallServerException
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
	}

	@Override
	public UndoItem getInverseOperation(PadDeleteContentRequest request)
	{
		final Pad pad = projectController.getPad(request.getPadId());
		if(pad.getContent() == null)
		{
			return null;
		}

		final RequestMessage newMediaRequest = switch(pad.getContent())
		{
			case AudioPadContent audioPadContent -> new PadNewMediaRequest(pad.getId(), audioPadContent.getMediaPath());
		};

		return new UndoItem("Kachel löschen", request, new CompoundRequest(
				List.of(
						newMediaRequest,
						new PadSettingsUpdateRequest(pad.getId(), padMapper.padToPadDto(pad))
				)
		));
	}
}
