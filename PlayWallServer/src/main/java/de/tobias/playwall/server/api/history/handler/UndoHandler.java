package de.tobias.playwall.server.api.history.handler;

import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.RequestHandlerFactory;
import de.tobias.playwall.server.api.history.UndoManager;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(UndoRequest.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class UndoHandler implements RequestHandler<UndoRequest>
{
	private final UndoManager undoManager;
	private final ApplicationContext context;

	@Override
	public Optional<ResponseMessage> handleRequest(UndoRequest ignored) throws IOException, PlayWallServerException
	{
		final RequestMessage undoOperation = undoManager.getUndoOperation();

		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(undoOperation.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + undoOperation.getClass().getSimpleName());
		}

		requestHandlerOptional.get().handleRequest(undoOperation);

		return Optional.empty();
	}
}
