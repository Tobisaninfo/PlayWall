package de.tobias.playwall.server.api.history.handler;

import de.tobias.playwall.common.api.history.RedoRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.history.UndoManager;
import de.tobias.playwall.server.net.*;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(RedoRequest.class)
@SuppressWarnings({"rawtypes", "unchecked", "java:S1871"})
class RedoHandler implements OneTimeActionRequestHandler<RedoRequest>
{
	private final UndoManager undoManager;
	private final ApplicationContext context;

	@Override
	public void handleRequest(RedoRequest ignored) throws IOException
	{
		final RequestMessage redoOperation = undoManager.getRedoOperation();

		if(redoOperation == null)
		{
			throw new IllegalStateException("No redo operation available");
		}

		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(redoOperation.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + redoOperation.getClass().getSimpleName());
		}

		final RequestHandler requestHandler = requestHandlerOptional.get();

		switch(requestHandler)
		{
			case UndoableRequestHandler handler ->
			{
				final Optional<UndoItem> undoItemOptional = handler.handleRequest(redoOperation);
				undoItemOptional.ifPresent(undoManager::replaceCurrentUndoOperation);
			}
			case OneTimeActionRequestHandler handler -> handler.handleRequest(redoOperation);
			case GetRequestHandler handler -> handler.handleRequest(redoOperation);
		}
	}
}
