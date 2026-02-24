package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;

public class PlayWallButton extends Button
{
	private final ObjectProperty<FontAwesomeType> icon = new SimpleObjectProperty<>();

	public PlayWallButton()
	{
		this(null, null);
	}

	public PlayWallButton(FontAwesomeType iconType)
	{
		this(null, iconType);
	}

	public PlayWallButton(String labelText, FontAwesomeType iconType)
	{
		this.setText(labelText);
		setIcon(iconType);
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

		final FontIcon icon = new FontIcon(iconType);
		icon.setSize(16);
		icon.setMouseTransparent(true);
		setGraphic(icon);
	}

	public ObjectProperty<FontAwesomeType> iconProperty()
	{
		return icon;
	}
}
