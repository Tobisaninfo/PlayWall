package de.tobias.playwall.common.net;

// Close Status Code: 4000 - 4999 --> For Application Use
// https://github.com/Luka967/websocket-close-codes

// https://www.eclipse.org/jetty/javadoc/current/org/eclipse/jetty/websocket/api/StatusCode.html
public enum WebSocketCloseStatus
{
	CLIENT_DISCONNECTED(1000),
	SHUTDOWN(1001),
	SERVER_CLOSED(1002),
	NO_STATUS(1005),
	WEB_SOCKET_EOF(1006);

	private final int code;

	WebSocketCloseStatus(int code)
	{
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}
}
