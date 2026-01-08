package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.common.api.project.PadPlayPositionUpdate;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadPlayPositionListener implements UpdateMessageEventListener<PadPlayPositionUpdate>
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadPlayPositionUpdate message)
	{
		for(PadPlayPositionUpdate.PadPlayPosition position : message.getPositions())
		{
			projectController.getPadController(position.padId()).setPosition(Duration.millis(position.millis()));

			final PadView padView = mainViewController.getPadViewForPadId(position.padId());
			if(padView != null)
			{
				Platform.runLater(padView::updateTimeNodes);
			}

		}
	}

	@Override
	public Class<PadPlayPositionUpdate> getMessageClass()
	{
		return PadPlayPositionUpdate.class;
	}
}
