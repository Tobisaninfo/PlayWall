package de.tobias.playwall.server.api.history.handler;

import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoManager;
import de.tobias.playwall.server.net.*;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(UndoRequest.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class UndoHandler implements OneTimeActionRequestHandler<UndoRequest>
{
	private final UndoManager undoManager;
	private final ApplicationContext context;

	@Override
	public void handleRequest(UndoRequest ignored) throws IOException, PlayWallServerException
	{
		final RequestMessage undoOperation = undoManager.getUndoOperation();

		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(undoOperation.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + undoOperation.getClass().getSimpleName());
		}

		final RequestHandler requestHandler = requestHandlerOptional.get();

		switch(requestHandler)
		{
			case UndoableRequestHandler handler -> handler.handleRequest(undoOperation);
			case OneTimeActionRequestHandler handler -> handler.handleRequest(undoOperation);
			case GetRequestHandler handler -> handler.handleRequest(undoOperation);
		}
	}
}
