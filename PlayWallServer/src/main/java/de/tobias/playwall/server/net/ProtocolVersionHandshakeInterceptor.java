package de.tobias.playwall.server.net;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Rejects the WebSocket handshake (HTTP 400, reason in {@link #REJECT_REASON_HEADER}) if the client does not send
 * a matching {@link #PROTOCOL_VERSION_HEADER}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolVersionHandshakeInterceptor implements HandshakeInterceptor
{
	public static final String PROTOCOL_VERSION_HEADER = "X-Protocol-Version";
	public static final String REJECT_REASON_HEADER = "X-Reject-Reason";

	/**
	 * Version of unpackaged builds (e.g. the Companion plugin during local development); always accepted.
	 */
	private static final String DEV_VERSION = "0.0.0";

	private final BuildProperties buildProperties;

	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes)
	{
		final String clientVersion = request.getHeaders().getFirst(PROTOCOL_VERSION_HEADER);
		if(clientVersion == null || clientVersion.isBlank())
		{
			return reject(request, response, "Missing header field " + PROTOCOL_VERSION_HEADER);
		}

		final String serverVersion = buildProperties.getVersion();
		if(!DEV_VERSION.equals(clientVersion) && !clientVersion.equals(serverVersion))
		{
			return reject(request, response, "Version mismatch: client " + clientVersion + ", server " + serverVersion);
		}
		return true;
	}

	@Override
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception)
	{
		// Nothing to do
	}

	private boolean reject(ServerHttpRequest request, ServerHttpResponse response, String reason)
	{
		log.warn("Reject client {}: {}", request.getRemoteAddress(), reason);
		response.setStatusCode(HttpStatus.BAD_REQUEST);
		response.getHeaders().set(REJECT_REASON_HEADER, reason);
		return false;
	}
}
