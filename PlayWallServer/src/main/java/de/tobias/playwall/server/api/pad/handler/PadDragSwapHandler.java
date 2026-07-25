package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadDragSwapRequest;
import de.tobias.playwall.common.api.pad.update.PadSwapUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PadDragSwapRequest.class)
class PadDragSwapHandler extends UndoableRequestHandler<PadDragSwapRequest>
{
	private final ProjectController projectController;

	PadDragSwapHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController)
	{
		super(messageSource, context);
		this.projectController = projectController;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadDragSwapRequest requestMessage) throws IOException
	{
		final Pad pad1 = projectController.getPad(requestMessage.getPadId1());
		final Pad pad2 = projectController.getPad(requestMessage.getPadId2());

		projectController.swapPad(pad1, pad2);

		context.publishEvent(new PadSwapUpdate(pad1.getId(), pad2.getId()));

		final String shortMessage = messageSource.getMessage("undo.description.short.pad.drag.swap.replace", new Object[]{}, LocaleContextHolder.getLocale());
		final String longMessage = messageSource.getMessage("undo.description.long.pad.drag.swap.replace", new Object[]{}, LocaleContextHolder.getLocale());
		return Optional.of(new UndoItem(shortMessage, longMessage, requestMessage, new PadDragSwapRequest(requestMessage.getPadId2(), requestMessage.getPadId1())));
	}
}
