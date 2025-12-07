package de.tobias.playwall.server.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.UpdateMessage;
import de.tobias.playwall.server.SystemTrayHandler;
import de.tobias.playwall.server.api.PlayWallServerException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
	private final SystemTrayHandler systemTrayHandler;

	@EventListener(UpdateMessage.class)
	void handleUpdateMessageEvents(UpdateMessage message) throws JsonProcessingException
	{
		final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(message));
		sendToClients(textResponse, SESSIONS);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session)
	{
		log.debug("Client connection established to {}", session.getRemoteAddress());
		SESSIONS.add(session);
		systemTrayHandler.setNumberOfConnectedClients(SESSIONS.size());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status)
	{
		log.debug("Client connection closed to {} for reason {}", session.getRemoteAddress(), status);
		SESSIONS.remove(session);
		systemTrayHandler.setNumberOfConnectedClients(SESSIONS.size());
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

			final TextMessage textResponse;
			if(responseMessageOptional.isPresent())
			{
				textResponse = new TextMessage(objectMapper.writeValueAsString(responseMessageOptional.get()));
			}
			else
			{
				textResponse = new TextMessage(objectMapper.writeValueAsString(new ResponseMessage(parsedMessage.getMessageId())));
			}
			sendToClients(textResponse, List.of(session)); // TODO: Do not send to all clients, only updates should be sent to all clients
		}
		catch(PlayWallServerException e)
		{
			final ErrorMessage errorMessage = new ErrorMessage(parsedMessage.getMessageId(), e.getMessage(), e.getError());
			final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(errorMessage));
			sendToClients(textResponse, List.of(session)); // TODO: Do not send to all clients
		}
		catch(Exception e)
		{
			final ErrorMessage errorMessage = new ErrorMessage(parsedMessage.getMessageId(), e.getMessage(), null);
			final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(errorMessage));
			sendToClients(textResponse, List.of(session));  // TODO: Do not send to all clients
			log.error("Error processing request", e);
		}
	}

	private synchronized static void sendToClients(TextMessage textResponse, Collection<WebSocketSession> sessions)
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
				catch(Exception e)
				{
					log.error("Error on sending message: {}", textResponse.getPayload(), e);
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
