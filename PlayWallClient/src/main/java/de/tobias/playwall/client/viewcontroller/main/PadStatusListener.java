package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.model.project.Pad;
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
		final PadStatus clientStatus = PadStatus.fromPadControllerStatus(message.getStatus());
		final Pad pad = mainViewController.getProject().getPad(message.getPadId());
		pad.setStatus(clientStatus);

		final PadView padView = mainViewController.getPadViewForPadId(message.getPadId());
		if(padView != null)
		{
			padView.updateStatus(clientStatus);
		}
	}

	@Override
	public Class<PadStatusUpdate> getMessageClass()
	{
		return PadStatusUpdate.class;
	}
}
