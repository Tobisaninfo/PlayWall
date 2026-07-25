package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.pad.request.PadDragMoveRequest;
import de.tobias.playwall.common.api.pad.request.PadSetRequest;
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
import java.util.UUID;

@RequestHandlerTyped(PadDragMoveRequest.class)
class PadDragMoveHandler extends UndoableRequestHandler<PadDragMoveRequest>
{
	private final ProjectController projectController;
	private final PadMapper padMapper;

	PadDragMoveHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, PadMapper padMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.padMapper = padMapper;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadDragMoveRequest requestMessage) throws IOException
	{
		final Pad sourcePad = projectController.getPad(requestMessage.getSourcePadId());
		final Pad targetPad = projectController.getPad(requestMessage.getTargetPadId());

		final Pad newPadForSource = Pad.builder().id(UUID.randomUUID()).build();

		projectController.replacePad(newPadForSource, sourcePad);
		projectController.replacePad(sourcePad, targetPad);

		if(sourcePad.getContent() != null)
		{
			final PadController sourcePadController = projectController.getPadController(sourcePad.getId());
			sourcePadController.load();
		}

		context.publishEvent(new PadReplaceUpdate(padMapper.padToPadDto(newPadForSource), sourcePad.getId()));
		context.publishEvent(new PadReplaceUpdate(padMapper.padToPadDto(sourcePad), targetPad.getId()));

		final String shortMessage = messageSource.getMessage("undo.description.short.pad.drag.move.replace", new Object[]{}, LocaleContextHolder.getLocale());
		final String longMessage = messageSource.getMessage("undo.description.long.pad.drag.move.replace", new Object[]{}, LocaleContextHolder.getLocale());
		return Optional.of(new UndoItem(shortMessage, longMessage, requestMessage,
				new CompoundRequest(
						new PadSetRequest(sourcePad.getId(), padMapper.padToPadDto(targetPad)),
						new PadSetRequest(newPadForSource.getId(), padMapper.padToPadDto(sourcePad))
				)));
	}
}
