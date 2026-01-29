package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

import java.text.MessageFormat;

public class ColorButton extends Button
{
	private static final String STYLE_TEMPLATE = """
			    -fx-background-color: {0};
			    -fx-border-color: white;
			    -fx-border-width: 2px;
			    -fx-border-radius: 3px;
			    -fx-background-insets: 1px;
			""";

	public ColorButton()
	{
		final FontIcon icon = new FontIcon(FontAwesomeType.CIRCLE_ARROW_DOWN_SOLID);
		icon.setSize(14);
		icon.setMouseTransparent(true);
		this.setGraphic(icon);
		this.setAlignment(Pos.CENTER_RIGHT);
	}

	public void updateColors(ModernColor color)
	{
		this.setStyle(MessageFormat.format(STYLE_TEMPLATE, color.paint()));
	}
}
