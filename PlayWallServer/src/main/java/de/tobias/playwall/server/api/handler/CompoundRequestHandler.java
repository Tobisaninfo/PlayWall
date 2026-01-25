package de.tobias.playwall.server.api.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.net.RequestHandlerFactory;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(CompoundRequest.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class CompoundRequestHandler implements RequestHandler<CompoundRequest>
{
	private final ApplicationContext context;

	@Override
	public Optional<ResponseMessage> handleRequest(CompoundRequest compoundRequest) throws IOException, PlayWallServerException
	{
		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		for(RequestMessage requestMessage : compoundRequest.getRequests())
		{
			final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(requestMessage.getClass());
			if(requestHandlerOptional.isEmpty())
			{
				throw new IllegalArgumentException("Cannot handle request message type " + requestMessage.getClass().getSimpleName());
			}

			requestHandlerOptional.get().handleRequest(requestMessage);
		}
		return Optional.empty();
	}
}
