package de.tobias.playwall.client.domain.pad.view.desktop;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.view.desktop.listener.PadInputListener;
import javafx.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class PadEventDispatcher
{
	private final List<PadInputListener> padInputListeners = new ArrayList<>();

	public void addPadInputListener(PadInputListener listener)
	{
		this.padInputListeners.add(listener);
	}

	public void addFirstPadInputListener(PadInputListener listener)
	{
		this.padInputListeners.addFirst(listener);
	}

	public void removePadInputListener(PadInputListener listener)
	{
		this.padInputListeners.remove(listener);
	}

	public <T extends Event> void dispatchEvent(T event, Consumer<PadInputListener> onHandle)
	{
		for(PadInputListener listener : padInputListeners)
		{
			if(event.isConsumed())
			{
				break;
			}
			onHandle.accept(listener);
		}
	}
}
