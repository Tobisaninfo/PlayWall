package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import javafx.util.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadLoadedListener implements UpdateMessageEventListener<PadLoadedUpdate>
{
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadLoadedUpdate message)
	{
		final PadView padView = mainViewController.getPadViewForPadId(message.getPadId());
		if(padView != null)
		{
			padView.showLoading(!message.isLoaded());

			if(message.getDurationMillis() != null)
			{
				padView.setPadDuration(Duration.millis(message.getDurationMillis()));
			}
		}
	}

	@Override
	public Class<PadLoadedUpdate> getMessageClass()
	{
		return PadLoadedUpdate.class;
	}
}
