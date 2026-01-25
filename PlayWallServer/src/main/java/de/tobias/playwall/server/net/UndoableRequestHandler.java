package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoItem;

import java.io.IOException;
import java.util.Optional;

public non-sealed interface UndoableRequestHandler<T extends RequestMessage> extends RequestHandler
{
	Optional<UndoItem> handleRequest(T requestMessage) throws IOException, PlayWallServerException;
}
