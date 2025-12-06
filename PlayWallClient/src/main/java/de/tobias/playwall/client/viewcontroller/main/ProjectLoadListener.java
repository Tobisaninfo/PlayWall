package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;

public class ProjectLoadListener implements UpdateMessageEventListener<PadLoadedUpdate>
{
	@Override
	public void onUpdateMessage(PadLoadedUpdate message)
	{
		System.out.println(message);
	}

	@Override
	public Class<PadLoadedUpdate> getMessageClass()
	{
		return PadLoadedUpdate.class;
	}
}
