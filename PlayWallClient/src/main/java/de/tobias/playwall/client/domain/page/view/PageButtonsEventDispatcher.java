package de.tobias.playwall.client.domain.page.view;

import de.tobias.playwall.client.appcontext.Service;
import javafx.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class PageButtonsEventDispatcher
{
	private final List<PageButtonInputListener> padInputListeners = new ArrayList<>();

	public void addPageInputListener(PageButtonInputListener listener)
	{
		this.padInputListeners.add(listener);
	}

	public void removePageInputListener(PageButtonInputListener listener)
	{
		this.padInputListeners.remove(listener);
	}

	public <T extends Event> void dispatchEvent(T event, Consumer<PageButtonInputListener> onHandle)
	{
		for(PageButtonInputListener listener : padInputListeners)
		{
			if(event.isConsumed())
			{
				break;
			}
			onHandle.accept(listener);
		}
	}
}
