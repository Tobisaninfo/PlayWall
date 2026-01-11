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
		// Delete all items after the current cursor position (possible redo items get lost on new undo item)
		if(cursor + 1 < history.size())
		{
			history.subList(cursor + 1, history.size()).clear();
		}

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
