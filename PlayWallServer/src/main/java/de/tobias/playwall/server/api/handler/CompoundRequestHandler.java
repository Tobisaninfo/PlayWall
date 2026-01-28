package de.tobias.playwall.server.api.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.net.*;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(CompoundRequest.class)
@SuppressWarnings({"rawtypes", "unchecked", "java:S1871"})
class CompoundRequestHandler implements OneTimeActionRequestHandler<CompoundRequest>
{
	private final ApplicationContext context;

	@Override
	public void handleRequest(CompoundRequest compoundRequest) throws IOException, PlayWallServerException
	{
		final RequestHandlerFactory requestHandlerFactory = context.getBean(RequestHandlerFactory.class);
		for(RequestMessage requestMessage : compoundRequest.getRequests())
		{
			final Optional<RequestHandler> requestHandlerOptional = requestHandlerFactory.getRequestHandler(requestMessage.getClass());
			if(requestHandlerOptional.isEmpty())
			{
				throw new IllegalArgumentException("Cannot handle request message type " + requestMessage.getClass().getSimpleName());
			}
			final RequestHandler requestHandler = requestHandlerOptional.get();

			switch(requestHandler)
			{
				case UndoableRequestHandler handler -> handler.handleRequest(requestMessage);
				case OneTimeActionRequestHandler handler -> handler.handleRequest(requestMessage);
				case GetRequestHandler handler -> handler.handleRequest(requestMessage);
			}
		}
	}
}
