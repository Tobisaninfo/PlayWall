package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;

import java.io.IOException;

public non-sealed interface OneTimeActionRequestHandler<T extends RequestMessage> extends RequestHandler
{
	void handleRequest(T requestMessage) throws IOException, PlayWallServerException;
}
