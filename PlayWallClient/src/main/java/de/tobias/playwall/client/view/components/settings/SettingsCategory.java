package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.Getter;

public class SettingsCategory extends Button
{
	@Getter
	private final BaseSettingsViewController<?> settingsPageController;

	public SettingsCategory(String labelText, FontIconType iconType, BaseSettingsViewController<?> settingsPageController)
	{
		this.settingsPageController = settingsPageController;

		final FontIcon icon = new FontIcon(iconType);
		icon.setSize(20);
		icon.setMouseTransparent(true);

		final Label label = new Label(labelText);
		label.getStyleClass().add("settings-category-label");

		final Region spacer = new Region();

		final FontIcon iconWarning = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
		iconWarning.setSize(15);
		iconWarning.setMouseTransparent(true);
		iconWarning.getStyleClass().add("settings-category-icon-warning");
		iconWarning.visibleProperty().bind(settingsPageController.getIsValidProperty().not());

		final HBox box = new HBox(14);
		box.setAlignment(Pos.CENTER_LEFT);
		box.getChildren().addAll(icon, label, spacer, iconWarning);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		this.setGraphic(box);

		this.getStyleClass().add("settings-category");

		this.setMaxWidth(Double.MAX_VALUE);
	}
}
