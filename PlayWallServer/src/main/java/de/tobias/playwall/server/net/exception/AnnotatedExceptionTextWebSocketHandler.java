package de.tobias.playwall.server.net.exception;

import de.tobias.playwall.common.api.StackTraceError;
import de.tobias.playwall.common.net.BaseMessage;
import de.tobias.playwall.common.net.ErrorMessage;
import de.tobias.playwall.common.net.RequestMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("java:S6813")
public abstract class AnnotatedExceptionTextWebSocketHandler extends TextWebSocketHandler
{
	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private ApplicationContext context;

	private final Map<Class<? extends Throwable>, ExceptionHandlerMethod> mappings = new HashMap<>();

	@PostConstruct
	void init()
	{
		Map<String, Object> adviceBeans = context.getBeansWithAnnotation(WsExceptionAdvice.class);

		for(Object bean : adviceBeans.values())
		{
			for(Method method : bean.getClass().getDeclaredMethods())
			{
				if(method.isAnnotationPresent(WsExceptionHandler.class))
				{
					WsExceptionHandler ann = method.getAnnotation(WsExceptionHandler.class);
					for(Class<? extends Throwable> exClass : ann.value())
					{
						mappings.put(exClass, new ExceptionHandlerMethod(bean, method));
					}
				}
			}
		}
	}

	@Override
	public final void handleTextMessage(WebSocketSession session, TextMessage message)
	{
		log.debug("Received: {}", message.getPayload());
		final RequestMessage requestMessage = objectMapper.readValue(message.getPayload(), RequestMessage.class);
		try
		{
			doHandleTextMessage(session, requestMessage);
		}
		catch(Exception e)
		{
			final ErrorMessage errorMessage = dispatchException(requestMessage, e);
			if(errorMessage != null)
			{
				sendToClients(errorMessage, List.of(session));
			}
			else
			{
				final String stackTrace = ExceptionUtils.getStackTrace(e);
				sendToClients(new ErrorMessage(requestMessage.getMessageId(), e.getMessage(), new StackTraceError(stackTrace)), List.of(session));
				log.error("Error processing request", e);
			}
		}
	}

	@SuppressWarnings("java:S112")
	protected abstract void doHandleTextMessage(WebSocketSession session, RequestMessage requestMessage) throws Exception;

	@SuppressWarnings({"java:S3011", "java:S112"})
	private ErrorMessage dispatchException(RequestMessage requestMessage, Exception e)
	{
		Class<?> currentEx = e.getClass();
		while(currentEx != null && Throwable.class.isAssignableFrom(currentEx))
		{
			ExceptionHandlerMethod handler = mappings.get(currentEx);
			if(handler != null)
			{
				try
				{
					handler.method().setAccessible(true);
					return (ErrorMessage) handler.method().invoke(handler.bean(), requestMessage, e);
				}
				catch(Exception invokeException)
				{
					throw new RuntimeException(invokeException);
				}
			}
			currentEx = currentEx.getSuperclass();
		}
		return null;
	}

	protected synchronized void sendToClients(BaseMessage message, Collection<WebSocketSession> sessions)
	{
		final Marker marker = MarkerFactory.getMarker(message.getClass().getSimpleName());
		final TextMessage textResponse = new TextMessage(objectMapper.writeValueAsString(message));
		log.debug(marker, "Sending: {}", textResponse.getPayload());

		for(WebSocketSession webSocketSession : sessions)
		{
			if(webSocketSession.isOpen())
			{
				try
				{
					webSocketSession.sendMessage(textResponse);
				}
				catch(Exception e)
				{
					log.error("Error on sending message: {}", textResponse.getPayload(), e);
				}
			}
		}
	}
}
