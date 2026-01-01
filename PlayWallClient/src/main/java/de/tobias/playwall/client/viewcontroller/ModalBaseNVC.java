package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Base class for modal dialogs.
 *
 * @param <Result> Return type of the dialog.
 */
public abstract class ModalBaseNVC<Param, Result> extends BaseNVC
{
	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		if(AppContextHolder.getInstance().getEnvironment() != AppContext.Environment.GUI_TESTING)
		{
			stage.initModality(Modality.WINDOW_MODAL);
		}
		stageContainer.addCloseKeyShortcut(stageContainer::close);
	}

	protected Result getResultValue()
	{
		return null;
	}

	protected void initParams(Param param)
	{
	}

	public Optional<Result> showAndWait(Param param, Window owner)
	{
		initParams(param);
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
		return Optional.ofNullable(getResultValue());
	}
}
