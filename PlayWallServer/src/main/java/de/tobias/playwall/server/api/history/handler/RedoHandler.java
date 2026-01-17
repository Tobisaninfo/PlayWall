package de.tobias.playwall.server.api.history.handler;

import de.tobias.playwall.common.api.history.RedoRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.RequestHandlerFactory;
import de.tobias.playwall.server.history.UndoManager;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(RedoRequest.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class RedoHandler implements RequestHandler<RedoRequest>
{
	private final UndoManager undoManager;
	private final ApplicationContext context;

	@Override
	public Optional<ResponseMessage> handleRequest(RedoRequest ignored) throws IOException, PlayWallServerException
	{
		final RequestMessage redoOperation = undoManager.getRedoOperation();

		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(redoOperation.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + redoOperation.getClass().getSimpleName());
		}

		return requestHandlerOptional.get().handleRequest(redoOperation); // TODO: Should this really get returned
	}
}
