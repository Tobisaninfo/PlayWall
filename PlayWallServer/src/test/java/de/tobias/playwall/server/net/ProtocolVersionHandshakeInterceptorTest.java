package de.tobias.playwall.server.net;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

class ProtocolVersionHandshakeInterceptorTest
{
	private static final String SERVER_VERSION = "8.3.0";

	private ProtocolVersionHandshakeInterceptor interceptor;
	private ServerHttpRequest request;
	private ServerHttpResponse response;
	private HttpHeaders requestHeaders;
	private HttpHeaders responseHeaders;

	@BeforeEach
	void setUp()
	{
		final Properties properties = new Properties();
		properties.setProperty("version", SERVER_VERSION);
		interceptor = new ProtocolVersionHandshakeInterceptor(new BuildProperties(properties));

		requestHeaders = new HttpHeaders();
		responseHeaders = new HttpHeaders();
		request = mock(ServerHttpRequest.class);
		response = mock(ServerHttpResponse.class);
		when(request.getHeaders()).thenReturn(requestHeaders);
		when(response.getHeaders()).thenReturn(responseHeaders);
	}

	private boolean handshake()
	{
		return interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());
	}

	private void assertRejected(String expectedReason)
	{
		verify(response).setStatusCode(HttpStatus.BAD_REQUEST);
		assertEquals(expectedReason, responseHeaders.getFirst(ProtocolVersionHandshakeInterceptor.REJECT_REASON_HEADER));
	}

	@Test
	void acceptsMatchingVersion()
	{
		requestHeaders.set(ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER, SERVER_VERSION);

		assertTrue(handshake());
		verify(response, never()).setStatusCode(any());
		assertNull(responseHeaders.getFirst(ProtocolVersionHandshakeInterceptor.REJECT_REASON_HEADER));
	}

	@Test
	void acceptsDevVersion()
	{
		requestHeaders.set(ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER, "0.0.0");

		assertTrue(handshake());
		verify(response, never()).setStatusCode(any());
	}

	@Test
	void rejectsMissingHeader()
	{
		assertFalse(handshake());
		assertRejected("Missing header field " + ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER);
	}

	@Test
	void rejectsBlankHeader()
	{
		requestHeaders.set(ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER, "  ");

		assertFalse(handshake());
		assertRejected("Missing header field " + ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER);
	}

	@Test
	void rejectsVersionMismatch()
	{
		requestHeaders.set(ProtocolVersionHandshakeInterceptor.PROTOCOL_VERSION_HEADER, "8.2.0");

		assertFalse(handshake());
		assertRejected("Version mismatch: client 8.2.0, server " + SERVER_VERSION);
	}
}
