package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;

public interface UpdateMessageEventListener<T extends UpdateMessage>
{
	void onUpdateMessage(T message);

	Class<T> getMessageClass();
}
