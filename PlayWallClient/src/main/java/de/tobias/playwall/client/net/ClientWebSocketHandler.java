package de.tobias.playwall.client.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thecodelabs.logger.Logger;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.net.WebSocketCloseStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

// TODO: Singleton
public class ClientWebSocketHandler implements WebSocket.Listener
{
	private static final int THREAD_COUNT = 6;
	private static ClientWebSocketHandler instance;

	private final ObjectMapper objectMapper;
	private final MessageQueue messageQueue;

	private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
	private final HttpClient httpClient = HttpClient.newBuilder().executor(executorService).build();
	private WebSocket ws;

	private final List<WebSocketListener> listeners;

	private ClientWebSocketHandler()
	{
		this.listeners = new ArrayList<>();
		this.objectMapper = new ObjectMapper().findAndRegisterModules();
		this.messageQueue = MessageQueue.getInstance();
	}

	public static ClientWebSocketHandler getInstance()
	{
		if(instance == null)
		{
			instance = new ClientWebSocketHandler();
		}
		return instance;
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
		executorService.close();
	}

	public static void shutdown()
	{
		if(instance != null)
		{
			instance.disconnect();
		}
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
			final ResponseMessage message = objectMapper.readValue(data, ResponseMessage.class);

			final Optional<Consumer<ResponseMessage>> callback = messageQueue.dequeueCallback(message.getMessageId());
			callback.ifPresent(consumer -> consumer.accept(message));
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

	public <T extends ResponseMessage> boolean send(RequestMessage message, Consumer<T> onResponse)
	{
		MessageQueue.getInstance().enqueueCallback(message, onResponse);
		try
		{
			return send(objectMapper.writeValueAsString(message));
		}
		catch(JsonProcessingException e)
		{
			Logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public boolean send(String data)
	{
		if(ws.isOutputClosed())
		{
			return false;
		}

		Logger.debug("Send: " + data);
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
