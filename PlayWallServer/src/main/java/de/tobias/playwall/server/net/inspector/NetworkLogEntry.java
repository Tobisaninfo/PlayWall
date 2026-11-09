package de.tobias.playwall.server.net.inspector;

import java.util.UUID;

/**
 * One message seen by the server on a client connection.
 *
 * @param id            monotonically increasing sequence number
 * @param timestamp     epoch milliseconds
 * @param clientAddress {@code ip:port} of the client the message was received from / sent to
 * @param clientType    type of the client as announced in the handshake header {@code X-Client-Type} (e.g. {@code JAVAFX}, {@code COMPANION}), {@link NetworkLogService#UNKNOWN_CLIENT_TYPE} if absent
 * @param payload       raw JSON, cut to {@link NetworkLogService#MAX_PAYLOAD_CHARS} characters if too large
 * @param sizeBytes     size of the complete, untruncated payload
 * @param correlatedId  response/error only: id of the entry of the matching request (if still known)
 * @param durationMs    response/error only: time between receiving the request and sending the response
 */
public record NetworkLogEntry(long id,
							  long timestamp,
							  Direction direction,
							  Kind kind,
							  String messageType,
							  UUID messageId,
							  String clientAddress,
							  String clientType,
							  String payload,
							  int sizeBytes,
							  boolean truncated,
							  Long correlatedId,
							  Long durationMs)
{
	public enum Direction
	{
		/**
		 * Client to server
		 */
		IN,
		/**
		 * Server to client
		 */
		OUT
	}

	public enum Kind
	{
		REQUEST,
		RESPONSE,
		UPDATE,
		ERROR
	}
}
