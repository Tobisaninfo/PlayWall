package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class ParamDialogBase<P> extends ViewControllerBase implements ParamView<P>, CloseRequest
{
	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stageContainer.addCloseKeyShortcut(() -> {
			onCloseRequest();
			stageContainer.close();
		});
	}

	public void showAndWait(P param, Window owner)
	{
		initParameter(param);
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
	}
}

