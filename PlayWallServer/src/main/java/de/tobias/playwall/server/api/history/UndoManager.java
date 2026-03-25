package de.tobias.playwall.server.api.history;

import de.tobias.playwall.common.api.history.UndoHistoryUpdate;
import de.tobias.playwall.common.net.RequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UndoManager
{
	private final ApplicationContext context;
	private final MessageSource messageSource;

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

		publishHistoryStateUpdate(null);
	}

	public RequestMessage getUndoOperation()
	{
		if(cursor < 0 || history.isEmpty())
		{
			return null;
		}

		final UndoItem undoItem = history.get(cursor);
		final RequestMessage inverseRequest = undoItem.inverseRequest();
		cursor--;

		publishHistoryStateUpdate(messageSource.getMessage("undo.description.long.postfix.undo", new Object[]{undoItem.longDescription()}, LocaleContextHolder.getLocale()));

		return inverseRequest;
	}

	public void replaceCurrentUndoOperation(UndoItem undoItem)
	{
		if(cursor >= history.size())
		{
			return;
		}

		history.set(cursor, undoItem);
	}

	public RequestMessage getRedoOperation()
	{
		if(cursor >= history.size() - 1)
		{
			return null;
		}

		final UndoItem undoItem = history.get(cursor + 1);
		final RequestMessage request = undoItem.request();
		cursor++;

		publishHistoryStateUpdate(messageSource.getMessage("undo.description.long.postfix.redo", new Object[]{undoItem.longDescription()}, LocaleContextHolder.getLocale()));

		return request;
	}

	private void publishHistoryStateUpdate(String message)
	{
		final String nextUndoOperation = cursor < 0 ? null : history.get(cursor).shortDescription();
		final String nextRedoOperation = cursor >= history.size() - 1 ? null : history.get(cursor + 1).shortDescription();
		context.publishEvent(new UndoHistoryUpdate(nextUndoOperation, nextRedoOperation, message));
	}

	public void clear()
	{
		history.clear();
		cursor = 0;
	}
}
