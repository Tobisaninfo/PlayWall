package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadDragDuplicateRequest;
import de.tobias.playwall.common.api.pad.update.PadReplaceUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PadDragDuplicateRequest.class)
class PadDragDuplicateHandler extends UndoableRequestHandler<PadDragDuplicateRequest>
{
	private final ProjectController projectController;
	private final PadMapper padMapper;

	PadDragDuplicateHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, PadMapper padMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.padMapper = padMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadDragDuplicateRequest requestMessage) throws IOException
	{
		final Pad sourcePad = padMapper.padDtoToPad(requestMessage.getSourcePad());
		final Pad targetPad = projectController.getPad(requestMessage.getTargetPadId());

		final Pad copied = sourcePad.copy(true);
		projectController.replacePad(copied, targetPad);

		context.publishEvent(new PadReplaceUpdate(padMapper.padToPadDto(copied), targetPad.getId()));

		if(copied.getContent() != null)
		{
			final PadController copiedPadController = projectController.getPadController(copied.getId());
			copiedPadController.load();
		}

		final String shortMessage = messageSource.getMessage("undo.description.short.pad.drag.duplicate.replace", new Object[]{}, LocaleContextHolder.getLocale());
		final String longMessage = messageSource.getMessage("undo.description.long.pad.drag.duplicate.replace", new Object[]{}, LocaleContextHolder.getLocale());
		return Optional.of(new UndoItem(shortMessage, longMessage, requestMessage,
				new PadDragDuplicateRequest(padMapper.padToPadDto(targetPad), copied.getId())));
	}
}
