package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.model.project.PadStatus;
import de.tobias.playwall.common.api.project.PadStatusUpdate;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadStatusListener implements UpdateMessageEventListener<PadStatusUpdate>
{
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadStatusUpdate message)
	{
		final PadView padView = mainViewController.getPadViewForPadId(message.getPadId());
		if(padView != null)
		{
			padView.updateStatus(PadStatus.fromPadControllerStatus(message.getStatus()));
		}
	}

	@Override
	public Class<PadStatusUpdate> getMessageClass()
	{
		return PadStatusUpdate.class;
	}
}
