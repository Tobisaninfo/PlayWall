package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.components.EnumCell;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "GlobalVolumeActionSettingsView", applyToStage = false)
class GlobalVolumeActionSettingsViewController extends ActionSettingsViewController
{
	@FXML
	private ComboBox<GlobalVolumeAction.VolumeChangeMode> modeComboBox;

	@FXML
	private ComboBox<GlobalVolumeAction.VolumeChangeDelta> deltaComboBox;

	@Override
	protected void init()
	{
		modeComboBox.getItems().addAll(GlobalVolumeAction.VolumeChangeMode.values());
		modeComboBox.setCellFactory(_ -> new EnumCell<>(GlobalVolumeAction.VolumeChangeMode.class));
		modeComboBox.setButtonCell(new EnumCell<>(GlobalVolumeAction.VolumeChangeMode.class));

		deltaComboBox.getItems().addAll(GlobalVolumeAction.VolumeChangeDelta.values());
		deltaComboBox.setCellFactory(_ -> new EnumCell<>(GlobalVolumeAction.VolumeChangeDelta.class));
		deltaComboBox.setButtonCell(new EnumCell<>(GlobalVolumeAction.VolumeChangeDelta.class));
	}

	@Override
	public Action createNewAction()
	{
		return new GlobalVolumeAction(GlobalVolumeAction.VolumeChangeMode.INCREASE, GlobalVolumeAction.VolumeChangeDelta.FIVE);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof GlobalVolumeAction globalVolumeAction)
		{
			modeComboBox.getSelectionModel().select(globalVolumeAction.getVolumeChangeMode());
			deltaComboBox.getSelectionModel().select(globalVolumeAction.getDelta());
		}
	}

	@Override
	public void applySettings(Action action)
	{
		if(action instanceof GlobalVolumeAction globalVolumeAction)
		{
			globalVolumeAction.setVolumeChangeMode(modeComboBox.getSelectionModel().getSelectedItem());
			globalVolumeAction.setDelta(deltaComboBox.getSelectionModel().getSelectedItem());
		}
	}
}
