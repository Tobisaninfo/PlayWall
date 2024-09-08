package de.tobias.playwall.server.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.project.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ServerWebSocketHandler extends TextWebSocketHandler
{
	private static final Set<WebSocketSession> SESSIONS = new HashSet<>();

	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception
	{
		SESSIONS.add(session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
	{
		try
		{
			final RequestMessage parsedMessage = objectMapper.readValue(message.getPayload(), RequestMessage.class);

			final ResponseMessage responseMessage;
			if(parsedMessage instanceof ProjectListRequest request)
			{
				responseMessage = new ProjectListResponse(request.getMessageId(), List.of(
						new ProjectMetadata("abc", LocalDateTime.now()),
						new ProjectMetadata("def", LocalDateTime.now())
				));
			}
			else if(parsedMessage instanceof ProjectDeleteRequest request)
			{
				responseMessage = new ProjectDeleteResponse(request.getMessageId(), true);
			}
			else
			{
				throw new IllegalArgumentException("Cannot handle request message type " + parsedMessage.getClass().getSimpleName());
			}

			final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(responseMessage));

			for(WebSocketSession webSocketSession : SESSIONS)
			{
				if(webSocketSession.isOpen())
				{
					try
					{
						webSocketSession.sendMessage(textResponse);
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
}
