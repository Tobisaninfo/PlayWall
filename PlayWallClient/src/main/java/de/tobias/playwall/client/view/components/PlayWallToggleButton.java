package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;

public class PlayWallToggleButton extends ToggleButton
{
	private final ObjectProperty<FontAwesomeType> icon = new SimpleObjectProperty<>();

	public PlayWallToggleButton()
	{
		setContentDisplay(ContentDisplay.TOP);
	}

	public FontAwesomeType getIcon()
	{
		return icon.get();
	}

	public void setIcon(FontAwesomeType iconType)
	{
		this.icon.set(iconType);

		if(iconType == null)
		{
			setGraphic(null);
			return;
		}

		final FontIcon fontIcon = new FontIcon(iconType);
		fontIcon.setSize(16);
		fontIcon.setMouseTransparent(true);
		setGraphic(fontIcon);
	}

	public ObjectProperty<FontAwesomeType> iconProperty()
	{
		return icon;
	}
}