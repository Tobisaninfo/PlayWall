package de.tobias.playwall.server.controller;

import de.tobias.playwall.server.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController
{
	@MessageMapping("/chat.register")
	@SendTo("/topic/public")
	public ChatMessage register(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor)
	{
		final Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
		if(sessionAttributes != null)
		{
			sessionAttributes.put("username", chatMessage.getSender());
		}
		return chatMessage;
	}

	@MessageMapping("/chat.send")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage)
	{
		return chatMessage;
	}
}