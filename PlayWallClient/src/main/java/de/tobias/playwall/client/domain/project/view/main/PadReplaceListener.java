package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.pad.update.PadReplaceUpdate;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PadReplaceListener
{
	private final ClientProjectController projectController;
	private final PadMapper padMapper;
	private final MainViewController mainViewController;

	@EventListener(PadReplaceUpdate.class)
	void onPadReplace(PadReplaceUpdate message)
	{
		final PadDto newPad = message.getNewPad();

		projectController.replacePad(padMapper.padDtoToPad(newPad), message.getOldPadId());

		final ClientPadController padController = projectController.getPadController(newPad.getId());
		final PadView padView = mainViewController.getPadViewForPadId(message.getOldPadId());
		if(padView != null)
		{
			Platform.runLater(() -> padView.updateFromPad(mainViewController.getCurrentPage().getPosition(), padController));
		}

		Platform.runLater(mainViewController::updateStyle);
	}
}
