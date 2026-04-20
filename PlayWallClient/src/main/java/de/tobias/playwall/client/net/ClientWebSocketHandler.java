package de.tobias.playwall.client.net;

import de.tobias.playwall.client.CommandLineOptions;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.event.UpdateMessageEventHandler;
import de.tobias.playwall.common.net.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Service
@Slf4j
class ClientWebSocketHandler implements WebSocket.Listener
{
	private final JsonMapper objectMapper;
	private final ResponseQueue responseQueue;

	private final HttpClient httpClient = HttpClient.newBuilder().build();
	private WebSocket ws;

	private final Object lock = new Object();

	private final UpdateMessageEventHandler updateMessageEventHandler;
	private final CommandLineOptions commandLineOptions;

	private final ObjectProperty<ConnectionState> connectionState = new SimpleObjectProperty<>();
	private volatile boolean intentionalDisconnect = false;

	@InjectConstructor
	ClientWebSocketHandler(UpdateMessageEventHandler updateMessageEventHandler, CommandLineOptions commandLineOptions)
	{
		this.updateMessageEventHandler = updateMessageEventHandler;
		this.commandLineOptions = commandLineOptions;
		this.objectMapper = JsonMapper.builder().findAndAddModules().build();
		this.responseQueue = new ResponseQueue();
	}

	public void connect(Map<String, String> headers)
	{
		intentionalDisconnect = false;
		var webSocketBuilder = httpClient.newWebSocketBuilder();

		final String url = "ws://localhost:" + commandLineOptions.getServerPort() + "/websocket";
		for(var entry : headers.entrySet())
		{
			webSocketBuilder = webSocketBuilder.header(entry.getKey(), entry.getValue());
		}

		this.ws = webSocketBuilder
				.buildAsync(URI.create(url), this)
				.join();
	}

	public void disconnect()
	{
		if(ws == null)
		{
			return;
		}

		intentionalDisconnect = true;
		ws.sendClose(WebSocket.NORMAL_CLOSURE, "Client closed connection");
		ws.abort();
		ws = null;

		httpClient.close();
	}

	@Override
	public void onOpen(WebSocket webSocket)
	{
		log.debug("Connected to websocket: {}", webSocket);
		WebSocket.Listener.super.onOpen(webSocket);
		connectionState.set(ConnectionState.CONNECTED);
	}

	private StringBuilder text = new StringBuilder();

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
	{
		text.append(data);

		if(last)
		{
			processInput(text.toString());
			text = new StringBuilder();
		}

		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	private void processInput(String data)
	{
		try
		{
			final BaseMessage message = objectMapper.readValue(data, BaseMessage.class);
			final Marker marker = MarkerFactory.getMarker(message.getClass().getSimpleName());
			log.debug(marker, "Received: {}", data);

			if(message instanceof ResponseMessage responseMessage)
			{
				synchronized(lock)
				{
					responseQueue.enqueueResponse(responseMessage.getMessageId(), responseMessage);
					log.trace("NotifyAll");
					lock.notifyAll();
				}
			}
			else if(message instanceof UpdateMessage updateMessage)
			{
				updateMessageEventHandler.fireEvent(updateMessage);
			}
		}
		catch(Exception e)
		{
			log.error("Cannot process input {}", data, e);
		}
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
	{
		if(statusCode == WebSocketCloseStatus.NO_STATUS.getCode())
		{
			log.debug("Disconnected: No status");
		}
		else if(statusCode == WebSocketCloseStatus.SHUTDOWN.getCode())
		{
			log.debug("Idle Timeout");
		}
		else if(statusCode == WebSocketCloseStatus.SERVER_CLOSED.getCode())
		{
			log.debug("Disconnected: connection refused");
		}
		else
		{
			log.debug("Disconnected");
		}

		if(!intentionalDisconnect)
		{
			connectionState.set(ConnectionState.DISCONNECTED);
		}

		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error)
	{
		log.error("Websocket on error", error);
		connectionState.set(ConnectionState.DISCONNECTED);
	}

	@SuppressWarnings({"unchecked", "java:S112"})
	public synchronized <T extends ResponseMessage> T send(RequestMessage message) throws PlayWallApiException
	{
		try
		{
			final String data = objectMapper.writeValueAsString(message);
			log.debug("Sending: {}", data);

			synchronized(lock)
			{
				send(data);

				Optional<ResponseMessage> messageOptional;
				while((messageOptional = this.responseQueue.dequeueResponse(message.getMessageId())).isEmpty())
				{
					log.trace("Waiting for response for message id {}", message.getMessageId());
					lock.wait(100L);
				}
				log.trace("Return response for message id {}", message.getMessageId());
				final ResponseMessage responseMessage = messageOptional.get();
				if(responseMessage instanceof ErrorMessage errorMessage)
				{
					throw new PlayWallApiException(errorMessage.getMessage(), errorMessage.getError());
				}

				return (T) responseMessage;
			}
		}
		catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	public boolean send(String data)
	{
		if(ws.isOutputClosed())
		{
			return false;
		}

		log.trace("Send: {}", data);
		ws.sendText(data, true);
		return true;
	}

	public ConnectionState getConnectionState()
	{
		return connectionState.get();
	}

	public ObjectProperty<ConnectionState> connectionStateProperty()
	{
		return connectionState;
	}

	void setConnectionState(ConnectionState state)
	{
		connectionState.set(state);
	}
}
