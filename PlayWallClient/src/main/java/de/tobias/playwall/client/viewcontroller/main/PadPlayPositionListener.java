package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.project.PadPlayPositionUpdate;
import javafx.util.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadPlayPositionListener implements UpdateMessageEventListener<PadPlayPositionUpdate>
{
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadPlayPositionUpdate message)
	{
		for(PadPlayPositionUpdate.PadPlayPosition position : message.getPositions())
		{
			final PadView padView = mainViewController.getPadViewForPadId(position.padId());
			if(padView != null)
			{
				padView.updatePlayPosition(Duration.millis(position.millis()));
			}

		}
	}

	@Override
	public Class<PadPlayPositionUpdate> getMessageClass()
	{
		return PadPlayPositionUpdate.class;
	}
}
