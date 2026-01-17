package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.model.project.PadStatus;
import de.tobias.playwall.client.service.ClientPadController;
import de.tobias.playwall.client.service.ClientProjectController;
import de.tobias.playwall.common.api.project.PadStatusUpdate;
import javafx.util.Duration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadStatusListener implements UpdateMessageEventListener<PadStatusUpdate>
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;

	@Override
	public void onUpdateMessage(PadStatusUpdate message)
	{
		final PadStatus clientStatus = PadStatus.fromPadControllerStatus(message.getStatus());
		final ClientPadController padController = projectController.getPadController(message.getPadId());
		padController.setStatus(clientStatus);

		if(clientStatus == PadStatus.READY)
		{
			padController.setPosition(Duration.ZERO);
		}

		if(clientStatus == PadStatus.EMPTY)
		{
			padController.setDuration(null);
			padController.setPosition(null);
		}

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
