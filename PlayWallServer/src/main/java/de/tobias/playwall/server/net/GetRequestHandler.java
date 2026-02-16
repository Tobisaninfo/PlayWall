package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;

import java.io.IOException;
import java.util.Optional;

/**
 * A RequestHandler to provide data as a return value to the client.
 */
public non-sealed interface GetRequestHandler<T extends RequestMessage> extends RequestHandler
{
	Optional<ResponseMessage> handleRequest(T requestMessage) throws IOException;
}
