package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class SettingsPage extends VBox
{
	private final PlayWallButton buttonCancel;
	private final PlayWallButton buttonSave;

	public SettingsPage(List<Region> settingsItems)
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, 0, ViewConstants.DEFAULT_SPACING));

		final VBox vboxSettingsItems = new VBox();
		vboxSettingsItems.setAlignment(Pos.TOP_LEFT);
		vboxSettingsItems.setSpacing(ViewConstants.DEFAULT_SPACING);
		vboxSettingsItems.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));
		vboxSettingsItems.getChildren().addAll(settingsItems);
		getChildren().add(vboxSettingsItems);

		final Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);
		getChildren().addAll(spacer);

		this.buttonCancel = new PlayWallButton(Localization.getString("ui.settings.button.cancel"), FontAwesomeType.TIMES);
		this.buttonSave = new PlayWallButton(Localization.getString("ui.settings.button.save"), FontAwesomeType.SAVE);

		final HBox boxButtons = new HBox(buttonCancel, buttonSave);
		boxButtons.setSpacing(ViewConstants.DEFAULT_SPACING);
		boxButtons.setAlignment(Pos.CENTER_RIGHT);
		getChildren().add(boxButtons);

		VBox.setVgrow(this, Priority.ALWAYS);

		Platform.runLater(this.buttonSave::requestFocus);
	}

	public void setButtonSaveText(String text)
	{
		this.buttonSave.setText(text);
	}

	public void disableButtonSave(boolean disable)
	{
		this.buttonSave.setDisable(disable);
	}
}
