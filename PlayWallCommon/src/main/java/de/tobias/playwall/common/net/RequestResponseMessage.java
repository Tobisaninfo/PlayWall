package de.tobias.playwall.common.net;

public class RequestResponseMessage extends Message
{
	public RequestResponseMessage()
	{
		this.setMessageType(MessageType.REQUEST_RESPONSE);
	}
}
