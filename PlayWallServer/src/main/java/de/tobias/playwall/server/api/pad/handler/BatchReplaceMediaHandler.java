package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.pad.request.BatchReplaceMediaRequest;
import de.tobias.playwall.common.api.pad.request.PadDeleteContentRequest;
import de.tobias.playwall.common.api.pad.request.PadNewMediaRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

@RequestHandlerTyped(BatchReplaceMediaRequest.class)
@Slf4j
class BatchReplaceMediaHandler implements UndoableRequestHandler<BatchReplaceMediaRequest>
{
	private final PadNewMediaHandler padNewMediaHandler;
	private final PadDeleteContentHandler padDeleteContentHandler;
	private final MessageSource messageSource;

	public BatchReplaceMediaHandler(ApplicationContext context, MessageSource messageSource)
	{
		this.messageSource = messageSource;
		this.padNewMediaHandler = context.getBean(PadNewMediaHandler.class);
		this.padDeleteContentHandler = context.getBean(PadDeleteContentHandler.class);
	}

	@Override
	public Optional<UndoItem> handleRequest(BatchReplaceMediaRequest requestMessage)
	{
		final List<RequestMessage> inverseOperations = new ArrayList<>();

		for(Map.Entry<UUID, String> entry : requestMessage.getNewMediaPathsByPadId().entrySet())
		{
			final Optional<UndoItem> undoItem = padNewMediaHandler.handleRequest(new PadNewMediaRequest(entry.getKey(), entry.getValue()));
			undoItem.ifPresent(item -> inverseOperations.add(item.inverseRequest()));
		}
		log.debug("Update media paths for {} pads", requestMessage.getNewMediaPathsByPadId().size());

		for(UUID padIdToDelete : requestMessage.getPadIdsToDelete())
		{
			final Optional<UndoItem> undoItem = padDeleteContentHandler.handleRequest(new PadDeleteContentRequest(padIdToDelete));
			undoItem.ifPresent(item -> inverseOperations.add(item.inverseRequest()));
		}
		log.debug("Deleted pad content for {} pads", requestMessage.getPadIdsToDelete());

		final String shortDescription = messageSource.getMessage("undo.description.short.media.replace", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.media.replace", new Object[]{inverseOperations.size()}, LocaleContextHolder.getLocale());
		return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new CompoundRequest(inverseOperations)));
	}
}
