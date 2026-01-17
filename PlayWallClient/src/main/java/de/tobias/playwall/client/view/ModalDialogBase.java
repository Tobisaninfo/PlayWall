package de.tobias.playwall.client.view;

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
 * @param <R> Return type of the dialog.
 */
public abstract class ModalDialogBase<R> extends ViewControllerBase
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

	protected R getResultValue()
	{
		return null;
	}

	public Optional<R> showAndWait(Window owner)
	{
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
		return Optional.ofNullable(getResultValue());
	}
}
