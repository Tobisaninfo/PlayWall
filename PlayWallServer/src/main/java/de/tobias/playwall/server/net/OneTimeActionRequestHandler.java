package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;

import java.io.IOException;

/**
 * Perform an action on the server that is not undoable. I can be performed multiple times.
 */
public non-sealed interface OneTimeActionRequestHandler<T extends RequestMessage> extends RequestHandler
{
	void handleRequest(T requestMessage) throws IOException;
}
