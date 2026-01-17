package de.tobias.playwall.server.net;

import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.Undoable;

import java.io.IOException;
import java.util.Optional;

public abstract class UndoableRequestHandler<T extends RequestMessage> implements RequestHandler<T>, Undoable<T>
{
	@Override
	public final Optional<ResponseMessage> handleRequest(T requestMessage) throws IOException, PlayWallServerException
	{
		handleUndoableRequest(requestMessage);
		return Optional.empty();
	}

	public abstract void handleUndoableRequest(T requestMessage) throws IOException, PlayWallServerException;
}
