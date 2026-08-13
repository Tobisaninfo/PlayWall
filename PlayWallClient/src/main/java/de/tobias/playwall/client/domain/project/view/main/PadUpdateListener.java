package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class PadUpdateListener implements UpdateMessageEventListener<PadUpdate>
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;
	private final PadMapper padMapper;

	@Override
	public void onUpdateMessage(PadUpdate message)
	{
		final Pad newPad = padMapper.padDtoToPad(message.getPad());
		final ClientPadController updatedPadController = projectController.updatePad(newPad);

		final PadView padView = mainViewController.getPadViewForPadId(message.getPad().getId());
		if(padView != null)
		{
			Platform.runLater(() -> padView.updateFromPad(projectController.getCurrentPage().getPosition(), updatedPadController));
		}

		Platform.runLater(mainViewController::updateStyle);
	}

	@Override
	public Class<PadUpdate> getMessageClass()
	{
		return PadUpdate.class;
	}
}
