package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import javafx.scene.control.Button;

public class PlayWallButton extends Button
{
	public PlayWallButton(FontIconType iconType)
	{
		this(null, iconType);
	}

	public PlayWallButton(String labelText, FontIconType iconType)
	{
		this.setText(labelText);

		final FontIcon icon = new FontIcon(iconType);
		icon.setSize(16);
		icon.setMouseTransparent(true);
		this.setGraphic(icon);
	}
}
