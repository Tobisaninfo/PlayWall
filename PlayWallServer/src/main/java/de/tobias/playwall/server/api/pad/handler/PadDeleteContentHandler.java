package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.pad.request.PadDeleteContentRequest;
import de.tobias.playwall.common.api.pad.request.PadNewMediaRequest;
import de.tobias.playwall.common.api.pad.request.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Optional;

@RequestHandlerTyped(PadDeleteContentRequest.class)
class PadDeleteContentHandler extends UndoableRequestHandler<PadDeleteContentRequest>
{
	private final ProjectController projectController;
	private final PadMapper padMapper;

	PadDeleteContentHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, PadMapper padMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.padMapper = padMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadDeleteContentRequest requestMessage)
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

		final PadController oldController = projectController.getPadController(requestMessage.getPadId());
		if(oldController != null)
		{
			oldController.unload();
		}

		final Pad pad = projectController.getPad(requestMessage.getPadId());

		pad.setContent(null);
		pad.setName(null);
		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));

		return Optional.ofNullable(inverseOperation);
	}

	private UndoItem getInverseOperation(PadDeleteContentRequest request)
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

		final String shortDescription = messageSource.getMessage("undo.description.short.pad.delete", new Object[]{}, LocaleContextHolder.getLocale());
		final String pageName = projectController.getPageByPad(pad.getId()).getSettings().getName();
		final String longDescription = messageSource.getMessage("undo.description.long.pad.delete", new Object[]{pad.getPosition() + 1, pageName}, LocaleContextHolder.getLocale());

		return new UndoItem(shortDescription, longDescription, request, new CompoundRequest(
				newMediaRequest,
				new PadSettingsUpdateRequest(pad.getId(), padMapper.padToPadDto(pad))
		));
	}
}
