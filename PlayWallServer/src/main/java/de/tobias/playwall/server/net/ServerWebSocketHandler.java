package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.UpdateMessage;
import de.tobias.playwall.server.SystemTrayHandler;
import de.tobias.playwall.server.net.exception.AnnotatedExceptionTextWebSocketHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"java:S3740"})
public class ServerWebSocketHandler extends AnnotatedExceptionTextWebSocketHandler
{
	private static final Set<WebSocketSession> SESSIONS = new HashSet<>();

	private final SystemTrayHandler systemTrayHandler;
	private final RequestExecutor requestExecutor;

	private final ProjectController controller;

	private boolean isShutdown = false;

	@EventListener(UpdateMessage.class)
	void handleUpdateMessageEvents(UpdateMessage message)
	{
		sendToClients(message, SESSIONS);
	}

	@EventListener(ContextClosedEvent.class)
	void shutdown()
	{
		isShutdown = true;
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

		if(!isShutdown)
		{
			updateSystemTray();
		}

		if(SESSIONS.isEmpty())
		{
			controller.stopAll(true);
		}
	}

	@Override
	protected void doHandleTextMessage(WebSocketSession session, RequestMessage requestMessage) throws Exception
	{
		final Optional<ResponseMessage> responseMessageOptional = requestExecutor.execute(requestMessage);
		sendToClients(responseMessageOptional.orElse(new ResponseMessage(requestMessage.getMessageId())), List.of(session));
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

					return MessageFormat.format("{0}:{1}", remoteAddress.getAddress().getHostAddress(), String.valueOf(remoteAddress.getPort()));
				})
				.toList());
	}
}
