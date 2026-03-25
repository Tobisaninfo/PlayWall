package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.pad.request.BatchColorPadsRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequestHandlerTyped(BatchColorPadsRequest.class)
class BatchColorPadsHandler implements UndoableRequestHandler<BatchColorPadsRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final MessageSource messageSource;

	public BatchColorPadsHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<UndoItem> handleRequest(BatchColorPadsRequest requestMessage)
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

		for(Map.Entry<UUID, Color> entry : requestMessage.getPadColors().entrySet())
		{
			final Pad pad = projectController.getPad(entry.getKey());
			pad.setDefaultColor(entry.getValue());
			context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));
		}

		return Optional.of(inverseOperation);
	}

	private UndoItem getInverseOperation(BatchColorPadsRequest requestMessage)
	{
		final Map<UUID, Color> previousColors = new HashMap<>();
		for(UUID padId : requestMessage.getPadColors().keySet())
		{
			final Pad pad = projectController.getPad(padId);
			previousColors.put(padId, pad.getDefaultColor());
		}

		final String shortDescription = messageSource.getMessage("undo.description.short.pad.color", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.pad.color", new Object[]{}, LocaleContextHolder.getLocale());
		return new UndoItem(shortDescription, longDescription, requestMessage, new BatchColorPadsRequest(previousColors));
	}
}
