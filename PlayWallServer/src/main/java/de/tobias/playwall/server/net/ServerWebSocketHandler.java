package de.tobias.playwall.server.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@AllArgsConstructor
@SuppressWarnings({"java:S3740", "rawtypes", "unchecked"})
public class ServerWebSocketHandler extends TextWebSocketHandler
{
	private static final Set<WebSocketSession> SESSIONS = new HashSet<>();

	private final ObjectMapper objectMapper;
	private final List<RequestHandler> requestHandlers;

	@Override
	public void afterConnectionEstablished(WebSocketSession session)
	{
		log.debug("Client connection established to {}", session.getRemoteAddress());
		SESSIONS.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status)
	{
		log.debug("Client connection closed to {} for reason {}", session.getRemoteAddress(), status);
		SESSIONS.remove(session);
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws JsonProcessingException
	{
		final RequestMessage parsedMessage = objectMapper.readValue(message.getPayload(), RequestMessage.class);
		log.debug("Received: {}", message.getPayload());

		try
		{
			final Optional<RequestHandler> requestHandlerOptional = getRequestHandler(parsedMessage.getClass());
			if(requestHandlerOptional.isEmpty())
			{
				throw new IllegalArgumentException("Cannot handle request message type " + parsedMessage.getClass().getSimpleName());
			}

			final RequestHandler requestHandler = requestHandlerOptional.get();
			final Optional<ResponseMessage> responseMessageOptional = requestHandler.handleRequest(parsedMessage);

			if(responseMessageOptional.isPresent())
			{
				final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(responseMessageOptional.get()));
				sendToClients(textResponse, List.of(session));
			}
		}
		catch(PlayWallServerException e)
		{
			final ErrorMessage errorMessage = new ErrorMessage(parsedMessage.getMessageId(), e.getMessage(), e.getError());
			final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(errorMessage));
			sendToClients(textResponse, List.of(session));
		}
		catch(IOException e)
		{
			// TODO: Return Error Messages
			log.error("Error processing request", e);
		}
	}

	private static void sendToClients(TextMessage textResponse, List<WebSocketSession> sessions)
	{
		log.debug("Sending: {}", textResponse.getPayload());

		for(WebSocketSession webSocketSession : sessions)
		{
			if(webSocketSession.isOpen())
			{
				try
				{
					webSocketSession.sendMessage(textResponse);
				}
				catch(IOException e)
				{
					log.error("Error on sending message", e);
				}
			}
		}
	}

	private Optional<RequestHandler> getRequestHandler(Class<? extends RequestMessage> requestClass)
	{
		return requestHandlers.stream().filter(handler -> {
			final RequestHandlerTyped annotation = AnnotationUtils.findAnnotation(handler.getClass(), RequestHandlerTyped.class);
			return Objects.equals(requireNonNull(annotation).value(), requestClass);
		}).findAny();
	}
}
