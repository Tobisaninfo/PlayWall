package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;

import java.io.IOException;
import java.util.Optional;

public interface RequestHandler<T extends RequestMessage>
{
	Optional<ResponseMessage> handleRequest(T requestMessage) throws IOException;
}
