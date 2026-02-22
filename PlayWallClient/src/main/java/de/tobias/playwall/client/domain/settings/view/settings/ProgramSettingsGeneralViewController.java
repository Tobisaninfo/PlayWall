package de.tobias.playwall.client.domain.settings.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsGeneralPageView", applyToStage = false)
public class ProgramSettingsGeneralViewController extends BaseProgramSettingsViewController
{
	@FXML
	private CheckBox checkboxStartAutoLoadLatestProject;

	@InjectConstructor
	public ProgramSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		checkboxStartAutoLoadLatestProject.setSelected(param.getSettings().isAutoLoadLatestProjectOnStart());
		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
		param.getSettings().setAutoLoadLatestProjectOnStart(checkboxStartAutoLoadLatestProject.isSelected());
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
