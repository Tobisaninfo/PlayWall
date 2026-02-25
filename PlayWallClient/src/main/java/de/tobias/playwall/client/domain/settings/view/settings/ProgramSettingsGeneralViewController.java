package de.tobias.playwall.client.domain.settings.view.settings;

import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.EnumCell;
import de.tobias.playwall.common.api.settings.model.UnsavedChangesMode;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsGeneralPageView", applyToStage = false)
public class ProgramSettingsGeneralViewController extends BaseProgramSettingsViewController
{
	@FXML
	private CheckBox checkboxStartAutoLoadLatestProject;

	@FXML
	private ComboBox<UnsavedChangesMode> comboBoxUnsavedChanges;

	@InjectConstructor
	public ProgramSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	protected void init()
	{
		comboBoxUnsavedChanges.getItems().addAll(UnsavedChangesMode.values());
		comboBoxUnsavedChanges.setButtonCell(new EnumCell<>(Strings.UI_SETTINGS_UNSAVED_CHANGES_MODE_BASE));
		comboBoxUnsavedChanges.setCellFactory(_ -> new EnumCell<>(Strings.UI_SETTINGS_UNSAVED_CHANGES_MODE_BASE));
	}

	@Override
	public void initParameter(Param param)
	{
		checkboxStartAutoLoadLatestProject.setSelected(param.getSettings().isAutoLoadLatestProjectOnStart());
		comboBoxUnsavedChanges.getSelectionModel().select(param.getSettings().getUnsavedChangesMode());

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getSettings().setAutoLoadLatestProjectOnStart(checkboxStartAutoLoadLatestProject.isSelected());
		param.getSettings().setUnsavedChangesMode(comboBoxUnsavedChanges.getSelectionModel().getSelectedItem());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
