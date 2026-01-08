package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadLoadedListener implements UpdateMessageEventListener<PadLoadedUpdate>
{
	private final ClientProjectController controller;
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadLoadedUpdate message)
	{
		final PadView padView = mainViewController.getPadViewForPadId(message.getPadId());

		// Update duration
		if(message.getDurationMillis() != null)
		{
			final Duration duration = Duration.millis(message.getDurationMillis());
			controller.getPadController(message.getPadId()).setDuration(duration);

			if(padView != null)
			{
				Platform.runLater(() -> padView.setPadDuration(duration));
			}
		}

		// Show loading state
		if(padView != null)
		{
			padView.showLoading(!message.isLoaded());
		}
	}

	@Override
	public Class<PadLoadedUpdate> getMessageClass()
	{
		return PadLoadedUpdate.class;
	}
}
