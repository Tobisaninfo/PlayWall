package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.ui.NVCStage;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Base class for modal dialogs.
 *
 * @param <T> Return type of the dialog.
 */
public abstract class ModalBaseNVC<T> extends BaseNVC
{
	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stage.initModality(Modality.WINDOW_MODAL);
		stageContainer.addCloseKeyShortcut(stageContainer::close);
	}

	protected T getResultValue()
	{
		return null;
	}

	public Optional<T> showAndWait(Window owner)
	{
		getStageContainer().ifPresent(nvcStage -> nvcStage.initOwner(owner).showAndWait());
		return Optional.ofNullable(getResultValue());
	}
}
