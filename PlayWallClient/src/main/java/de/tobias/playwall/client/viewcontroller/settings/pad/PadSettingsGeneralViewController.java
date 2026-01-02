package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsGeneralPageView", applyToStage = false)
public class PadSettingsGeneralViewController extends BasePadSettingsGeneralViewController
{
	@FXML
	private TextField textFieldName;

	@InjectConstructor
	public PadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}

	@Override
	public void initParameter(BasePadSettingsGeneralViewController.Param param)
	{
		textFieldName.setText(param.pad.getName());
		this.isValidProperty.bind(textFieldName.textProperty().isEmpty().not());
	}

	@Override
	public void applySettings(BasePadSettingsGeneralViewController.Param param)
	{
		param.pad.setName(textFieldName.getText());
	}
}
