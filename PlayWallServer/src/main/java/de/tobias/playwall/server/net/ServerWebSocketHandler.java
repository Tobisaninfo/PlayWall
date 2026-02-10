package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.UpdateMessage;
import de.tobias.playwall.server.SystemTrayHandler;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
@SuppressWarnings({"java:S3740"})
public class ServerWebSocketHandler extends TextWebSocketHandler
{
	private static final Set<WebSocketSession> SESSIONS = new HashSet<>();

	private final JsonMapper objectMapper;
	private final SystemTrayHandler systemTrayHandler;
	private final RequestExecutor requestExecutor;

	private final ProjectController controller;

	@EventListener(UpdateMessage.class)
	void handleUpdateMessageEvents(UpdateMessage message)
	{
		final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(message));
		sendToClients(textResponse, SESSIONS);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session)
	{
		log.debug("Client connection established to {}", session.getRemoteAddress());
		SESSIONS.add(session);
		updateSystemTray();
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status)
	{
		log.debug("Client connection closed to {} for reason {}", session.getRemoteAddress(), status);
		SESSIONS.remove(session);
		updateSystemTray();

		if(SESSIONS.isEmpty())
		{
			controller.stopAll();
		}
	}

	@Override
	protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
	{
		final RequestMessage parsedMessage = objectMapper.readValue(message.getPayload(), RequestMessage.class);
		log.debug("Received: {}", message.getPayload());

		try
		{
			final Optional<ResponseMessage> responseMessageOptional = requestExecutor.execute(parsedMessage);
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

	private static synchronized void sendToClients(TextMessage textResponse, Collection<WebSocketSession> sessions)
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

	private void updateSystemTray()
	{
		systemTrayHandler.updateConnectedClients(SESSIONS
				.stream()
				.map(s -> {
					final InetSocketAddress remoteAddress = s.getRemoteAddress();
					if(remoteAddress == null)
					{
						return "Unbekannt";
					}

					return MessageFormat.format("{0}:{1}", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
				})
				.toList());
	}
}
