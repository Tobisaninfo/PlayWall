package de.tobias.playwall.client.event;

import de.tobias.playwall.common.net.UpdateMessage;

interface ListenerInstance
{
	void execute(UpdateMessage updateMessage);
}
