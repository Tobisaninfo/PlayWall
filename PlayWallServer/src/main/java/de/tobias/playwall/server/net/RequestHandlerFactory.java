package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
@SuppressWarnings({"rawtypes"})
public class RequestHandlerFactory
{
	private final List<RequestHandler> requestHandlers;

	public Optional<RequestHandler> getRequestHandler(Class<? extends RequestMessage> requestClass)
	{
		return requestHandlers.stream().filter(handler -> {
			final RequestHandlerTyped annotation = AnnotationUtils.findAnnotation(handler.getClass(), RequestHandlerTyped.class);
			return Objects.equals(requireNonNull(annotation).value(), requestClass);
		}).findAny();
	}
}
