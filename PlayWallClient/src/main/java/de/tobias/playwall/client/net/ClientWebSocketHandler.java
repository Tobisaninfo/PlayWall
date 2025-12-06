package de.tobias.playwall.client.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.common.net.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

class ClientWebSocketHandler implements WebSocket.Listener
{
	private final ObjectMapper objectMapper;
	private final ResponseQueue responseQueue;

	private final HttpClient httpClient = HttpClient.newBuilder().build();
	private WebSocket ws;

	private final Object lock = new Object();
	private final List<WebSocketListener> listeners;

	ClientWebSocketHandler()
	{
		this.listeners = new ArrayList<>();
		this.objectMapper = new ObjectMapper().findAndRegisterModules();
		this.responseQueue = new ResponseQueue();
	}

	public void connect(Map<String, String> headers)
	{
		var webSocketBuilder = httpClient.newWebSocketBuilder();

		final String url = "ws://localhost:10023/websocket";
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

		ws.sendClose(WebSocket.NORMAL_CLOSURE, "Client closed connection");
		ws.abort();
		ws = null;

		httpClient.close();
	}

	@Override
	public void onOpen(WebSocket webSocket)
	{
		Logger.debug("Connected to websocket: " + webSocket);
		WebSocket.Listener.super.onOpen(webSocket);
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
			Logger.debug("Received: " + data);
			final BaseMessage message = objectMapper.readValue(data, BaseMessage.class);

			if(message instanceof ResponseMessage responseMessage)
			{
				synchronized(lock)
				{
					responseQueue.enqueueResponse(responseMessage.getMessageId(), responseMessage);
					Logger.trace("NotifyAll");
					lock.notifyAll();
				}
			}
			else if(message instanceof UpdateMessage updateMessage)
			{

			}
		}
		catch(Exception e)
		{
			Logger.error(e);
		}
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
	{
		if(statusCode == WebSocketCloseStatus.NO_STATUS.getCode())
		{
			Logger.debug("Disconnected: No status");
		}
		else if(statusCode == WebSocketCloseStatus.SHUTDOWN.getCode())
		{
			Logger.debug("Idle Timeout");
		}
		else if(statusCode == WebSocketCloseStatus.SERVER_CLOSED.getCode())
		{
			Logger.debug("Disconnected: connection refused");
		}
		else
		{
			Logger.debug("Disconnected");
		}
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error)
	{
		Logger.error(error);
	}

	public boolean isInitialized()
	{
		return ws != null;
	}

	public synchronized <T extends ResponseMessage> T send(RequestMessage message) throws PlayWallApiException
	{
		try
		{
			final String data = objectMapper.writeValueAsString(message);
			Logger.debug("Sending: " + data);

			synchronized(lock)
			{
				send(data);

				Optional<ResponseMessage> messageOptional;
				while((messageOptional = this.responseQueue.dequeueResponse(message.getMessageId())).isEmpty())
				{
					Logger.trace("Waiting for response for message id " + message.getMessageId());
					lock.wait(100L);
				}
				Logger.trace("Return response for message id " + message.getMessageId());
				final ResponseMessage responseMessage = messageOptional.get();
				if(responseMessage instanceof ErrorMessage errorMessage)
				{
					throw new PlayWallApiException(errorMessage.getMessage(), errorMessage.getError());
				}

				return (T) responseMessage;
			}
		}
		catch(JsonProcessingException e)
		{
			Logger.error(e);
			throw new RuntimeException(e);
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

		Logger.trace("Send: " + data);
		ws.sendText(data, true);
		return true;
	}

	public void addListener(WebSocketListener webSocketListener)
	{
		this.listeners.add(webSocketListener);
	}

	public void removeListener(WebSocketListener webSocketListener)
	{
		this.listeners.remove(webSocketListener);
	}

	private void dispatch(Consumer<WebSocketListener> consumer)
	{
		this.listeners.forEach(consumer);
	}
}
