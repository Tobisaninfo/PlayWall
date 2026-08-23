package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.components.EnumCell;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "PadActionSettingsView", applyToStage = false)
class PadActionSettingsViewController extends ActionSettingsViewController
{
	@FXML
	private ComboBox<PadAction.PadActionMode> modeComboBox;

	@Override
	protected void init()
	{
		modeComboBox.getItems().addAll(PadAction.PadActionMode.values());
		final String modePrefix = PadAction.PadActionMode.class.getSimpleName() + ".";
		modeComboBox.setCellFactory(_ -> new EnumCell<>(modePrefix));
		modeComboBox.setButtonCell(new EnumCell<>(modePrefix));
	}

	@Override
	public Action createNewAction()
	{
		return new PadAction(PadAction.PadActionMode.PLAY_STOP);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof PadAction padAction)
		{
			modeComboBox.getSelectionModel().select(padAction.getPadActionMode());
		}
	}

	@Override
	public void applySettings(Action action)
	{
		if(action instanceof PadAction padAction)
		{
			padAction.setPadActionMode(modeComboBox.getSelectionModel().getSelectedItem());
		}
	}
}
