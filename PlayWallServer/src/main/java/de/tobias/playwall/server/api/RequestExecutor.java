package de.tobias.playwall.server.api;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
@SuppressWarnings({"java:S3740", "rawtypes", "unchecked"})
@Slf4j
public class RequestExecutor
{
	private final List<RequestHandler> requestHandlers;

	public Optional<ResponseMessage> execute(RequestMessage requestMessage) throws Exception
	{
		final Optional<RequestHandler> requestHandlerOptional = getRequestHandler(requestMessage.getClass());
		if(requestHandlerOptional.isEmpty())
		{
			throw new IllegalArgumentException("Cannot handle request message type " + requestMessage.getClass().getSimpleName());
		}

		final RequestHandler requestHandler = requestHandlerOptional.get();
		return requestHandler.handleRequest(requestMessage);
	}

	private Optional<RequestHandler> getRequestHandler(Class<? extends RequestMessage> requestClass)
	{
		return requestHandlers.stream().filter(handler -> {
			final RequestHandlerTyped annotation = AnnotationUtils.findAnnotation(handler.getClass(), RequestHandlerTyped.class);
			return Objects.equals(requireNonNull(annotation).value(), requestClass);
		}).findAny();
	}

}
