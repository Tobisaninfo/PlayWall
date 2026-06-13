package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.history.UndoManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@AllArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
@Slf4j
public class RequestExecutor
{
	private final UndoManager undoManager;
	private final RequestHandlerFactory requestHandlerFactory;

	public Optional<ResponseMessage> execute(RequestMessage requestMessage) throws IOException
	{
		final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(requestMessage.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + requestMessage.getClass().getSimpleName());
		}

		final RequestHandler requestHandler = requestHandlerOptional.get();

		return switch(requestHandler)
		{
			case UndoableRequestHandler handler ->
			{
				final Optional<UndoItem> undoItem = handler.handleRequest(requestMessage);
				// Add to undo manager if previous code does not throw any exception. This means the handler execution was successful.
				undoItem.ifPresent(undoManager::addUndoOperation);
				yield Optional.empty();
			}
			case OneTimeActionRequestHandler handler ->
			{
				handler.handleRequest(requestMessage);
				yield Optional.empty();
			}
			case GetRequestHandler handler -> handler.handleRequest(requestMessage);
		};
	}
}
