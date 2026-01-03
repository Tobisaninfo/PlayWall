package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.ui.NVCStage;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class ParamDialogBase<P> extends ViewControllerBase implements ParamView<P>
{
	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stageContainer.addCloseKeyShortcut(stageContainer::close);
	}

	public void showAndWait(P param, Window owner)
	{
		initParameter(param);
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
	}
}

