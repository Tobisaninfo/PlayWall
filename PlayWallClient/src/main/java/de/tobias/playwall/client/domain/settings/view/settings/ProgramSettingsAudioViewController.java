package de.tobias.playwall.client.domain.settings.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

/**
 * Viewcontroller for the general page in the program settings dialog.
 */
@ViewController(path = "de/tobias/playwall/client/view/settings/program", view = "ProgramSettingsAudioPageView", applyToStage = false)
public class ProgramSettingsAudioViewController extends BaseProgramSettingsViewController
{
	@FXML
	private ComboBox<String> comboBoxOutputDevices;

	@InjectConstructor
	public ProgramSettingsAudioViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(Param param)
	{
		comboBoxOutputDevices.getItems().setAll(param.getOutputDeviceNames());

		this.isValidProperty.set(true);
	}

	@Override
	public void applySettings(Param param)
	{
	}

	@Override
	public void cleanup()
	{
		// Nothing to do
	}
}
