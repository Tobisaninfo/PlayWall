package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.components.settings.SettingsPage;
import de.tobias.playwall.client.viewcontroller.ModalBaseNVC;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

@ViewController(path = "de/tobias/playwall/client/view/settings/pad", view = "PadSettingsView")
public class PadSettingsViewController extends ModalBaseNVC<Void>
{
	@FXML
	private SettingsPage settingsPage;

	@Getter(AccessLevel.NONE)
	private final FluentClient client;

	@Getter(AccessLevel.NONE)
	private Pad pad;

	private Stage stage;

	@InjectConstructor
	public PadSettingsViewController(FluentClient client)
	{
		this.client = client;
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);

		this.stage = stage;

		stage.setWidth(560);
		stage.setHeight(380);

		stage.setMinWidth(560);
		stage.setMinHeight(380);

		stage.setMaxWidth(560);
		stage.setMaxHeight(380);
	}

	public void setPad(Pad pad)
	{
		this.pad = pad;

		stage.setTitle(Localization.getString(Strings.UI_SETTINGS_PAD_TITLE, pad.getPosition(), pad.getName()));
	}

	@FXML
	private void finishButtonHandler(ActionEvent event)
	{
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event)
	{
		getStageContainer().ifPresent(NVCStage::close);
	}
}
