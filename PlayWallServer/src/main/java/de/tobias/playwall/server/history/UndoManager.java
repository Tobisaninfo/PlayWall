package de.tobias.playwall.server.history;

import de.tobias.playwall.common.api.history.UndoHistoryUpdate;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UndoManager
{
	private final ApplicationContext context;

	private final List<UndoItem> history = new ArrayList<>();
	private int cursor = -1;

	public void addUndoOperation(UndoItem undoItem)
	{
		// Delete all items after the current cursor position (possible redo items get lost on a new undo item)
		if(cursor + 1 < history.size())
		{
			history.subList(cursor + 1, history.size()).clear();
		}

		history.add(undoItem);
		cursor = history.size() - 1;

		publishHistoryStateUpdate();
	}

	public RequestMessage getUndoOperation()
	{
		if(cursor < 0 || history.isEmpty())
		{
			return null;
		}

		final RequestMessage inverseRequest = history.get(cursor).inverseRequest();
		cursor--;

		publishHistoryStateUpdate();

		return inverseRequest;
	}

	public RequestMessage getRedoOperation()
	{
		if(cursor >= history.size() - 1)
		{
			return null;
		}

		final RequestMessage request = history.get(cursor + 1).request();
		cursor++;

		publishHistoryStateUpdate();

		return request;
	}

	private void publishHistoryStateUpdate()
	{
		final String nextUndoOperation = cursor < 0 ? null : history.get(cursor).description();
		final String nextRedoOperation = cursor >= history.size() - 1 ? null : history.get(cursor + 1).description();
		context.publishEvent(new UndoHistoryUpdate(nextUndoOperation, nextRedoOperation));
	}

	void clear()
	{
		history.clear();
		cursor = 0;
	}
}
