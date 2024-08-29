package de.tobias.playwall.server.net;

import com.google.gson.Gson;
import de.tobias.playwall.common.net.Message;
import de.tobias.playwall.common.net.project.ProjectEventMessageType;
import de.tobias.playwall.common.net.project.ProjectMessage;
import de.tobias.playwall.common.utils.GsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ServerWebSocketHandler extends TextWebSocketHandler
{
	private static final Gson GSON;
	private static final Set<WebSocketSession> SESSIONS = new HashSet<>();

	static
	{
		GSON = GsonUtils.gson();
	}


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
			Message parsedMessage = GSON.fromJson(message.getPayload(), Message.class);
			switch(parsedMessage.getScope())
			{
				case PROJECT:
					System.out.println(message);

					final ProjectMessage response = new ProjectMessage(ProjectEventMessageType.LIST_PROJECTS_RESPONSE);
					response.addPayload(ProjectEventMessageType.ListProjectsProperties.PROJECTS, List.of("abc", "def"));

					final TextMessage textResponse = new TextMessage(GSON.toJson(response));

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

					break;
				default:
					throw new RuntimeException("Unknown scope: " + parsedMessage.getScope());
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
	}
}
