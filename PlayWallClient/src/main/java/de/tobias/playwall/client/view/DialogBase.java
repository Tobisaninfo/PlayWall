package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.NVCStage;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DialogBase extends ViewControllerBase
{
	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stageContainer.addCloseKeyShortcut(stageContainer::close);
	}

	public void showAndWait(Window owner)
	{
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
	}
}

