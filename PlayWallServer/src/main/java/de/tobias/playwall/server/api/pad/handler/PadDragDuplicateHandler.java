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
		final Pad sourcePad = projectController.getPad(requestMessage.getSourcePad());
		final Pad targetPad = projectController.getPad(requestMessage.getDestinationPad());

		final Pad copied = sourcePad.copy(true);
		projectController.replacePad(copied, targetPad);

		context.publishEvent(new PadReplaceUpdate(targetPad.getId(), padMapper.padToPadDto(copied)));

		final PadController copiedPadController = projectController.getPadController(copied.getId());
		copiedPadController.load();

		return Optional.empty();
	}
}
