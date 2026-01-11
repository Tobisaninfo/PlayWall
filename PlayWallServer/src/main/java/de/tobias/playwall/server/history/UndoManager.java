package de.tobias.playwall.server.history;

import de.tobias.playwall.common.net.RequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UndoManager
{
	private final List<UndoItem> history = new ArrayList<>();
	private int cursor = -1;

	public void addUndoOperation(UndoItem undoItem)
	{
		history.add(undoItem);
		cursor = history.size() - 1;
	}

	public RequestMessage getUndoOperation()
	{
		if(cursor < 0)
		{
			return null;
		}

		final RequestMessage inverseRequest = history.get(cursor).inverseRequest();
		cursor--;

		return inverseRequest;
	}
}
