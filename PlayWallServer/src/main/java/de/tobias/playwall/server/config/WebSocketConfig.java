package de.tobias.playwall.server.config;

import de.tobias.playwall.server.net.ProtocolVersionHandshakeInterceptor;
import de.tobias.playwall.server.net.ServerWebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer
{
	private final ServerWebSocketHandler serverWebSocketHandler;
	private final ProtocolVersionHandshakeInterceptor protocolVersionHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
	{
		registry.addHandler(serverWebSocketHandler, "/websocket")
				.addInterceptors(protocolVersionHandshakeInterceptor)
				.setAllowedOrigins("*");
	}

	@Bean
	@Profile("!test")
	public ServletServerContainerFactoryBean createWebSocketContainer()
	{
		final ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(2 * 1024 * 1024);
		container.setMaxBinaryMessageBufferSize(2 * 1024 * 1024);
		return container;
	}
}