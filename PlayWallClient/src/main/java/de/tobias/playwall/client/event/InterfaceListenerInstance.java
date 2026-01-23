package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;

@SuppressWarnings({"rawtypes", "unchecked"})
record InterfaceListenerInstance(UpdateMessageEventListener listener) implements ListenerInstance
{
	@Override
	public void execute(UpdateMessage updateMessage)
	{
		listener.onUpdateMessage(updateMessage);
	}
}
