package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.pad.update.PadSwapUpdate;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
class PadSwapListener
{
	private final ClientProjectController projectController;
	private final MainViewController mainViewController;

	@EventListener(PadSwapUpdate.class)
	void onPadReplace(PadSwapUpdate message)
	{
		projectController.swapPads(message.getPad1(), message.getPad2());

		updatePadView(message.getPad1(), message.getPad2());
		updatePadView(message.getPad2(), message.getPad1());

		Platform.runLater(mainViewController::updateStyle);
	}

	private void updatePadView(UUID pad1, UUID pad2)
	{
		final PadView padView = mainViewController.getPadViewForPadId(pad1);
		if(padView != null)
		{
			final ClientPadController padController = projectController.getPadController(pad2);
			Platform.runLater(() -> padView.updateFromPad(projectController.getCurrentPage().getPosition(), padController));
		}
	}
}
