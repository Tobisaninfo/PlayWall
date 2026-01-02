package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsViewController
{
	@FXML
	private TextField textFieldName;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(BasePadSettingsViewController.Param param)
	{
		textFieldName.setText(param.pad.getName());
		this.isValidProperty.bind(textFieldName.textProperty().isEmpty().not());
	}

	public void applySettings(Pad pad)
	{
		pad.setName(textFieldName.getText());
	}
}
