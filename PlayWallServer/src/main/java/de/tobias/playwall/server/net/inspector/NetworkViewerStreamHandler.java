package de.tobias.playwall.server.net.inspector;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pushes new {@link NetworkLogEntry}s to the open network viewer pages. These sessions are deliberately not part of
 * the client sessions of the PlayWall protocol and are therefore never logged themselves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NetworkViewerStreamHandler extends TextWebSocketHandler
{
	private static final int SEND_TIME_LIMIT_MS = 2000;
	private static final int BUFFER_SIZE_LIMIT = 1024 * 1024;

	private final NetworkLogService networkLogService;
	private final JsonMapper jsonMapper;

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	@PostConstruct
	void init()
	{
		networkLogService.addListener(this::broadcast);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session)
	{
		sessions.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status)
	{
		sessions.remove(session.getId());
	}

	private void broadcast(NetworkLogEntry entry)
	{
		if(sessions.isEmpty())
		{
			return;
		}
		final TextMessage message = new TextMessage(jsonMapper.writeValueAsString(entry));
		for(WebSocketSession session : sessions.values())
		{
			try
			{
				if(session.isOpen())
				{
					session.sendMessage(message);
				}
			}
			catch(Exception e)
			{
				log.debug("Could not send entry to network viewer {}", session.getId(), e);
				sessions.remove(session.getId());
			}
		}
	}
}
