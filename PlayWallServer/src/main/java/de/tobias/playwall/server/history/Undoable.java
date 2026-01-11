package de.tobias.playwall.server.history;

import de.tobias.playwall.common.net.RequestMessage;

public interface Undoable<T extends RequestMessage>
{
	UndoItem getInverseOperation(T request);
}
