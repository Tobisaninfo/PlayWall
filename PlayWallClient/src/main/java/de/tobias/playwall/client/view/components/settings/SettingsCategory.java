package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import javafx.scene.control.Button;

public class SettingsCategory extends Button
{
	public SettingsCategory(String labelText, FontIconType iconType)
	{
		this.setText(labelText);

		final FontIcon icon = new FontIcon(iconType);
		icon.setSize(20);
		icon.setMouseTransparent(true);
		this.setGraphic(icon);

		this.getStyleClass().add("settings-category");

		this.setMaxWidth(Double.MAX_VALUE);
	}
}
