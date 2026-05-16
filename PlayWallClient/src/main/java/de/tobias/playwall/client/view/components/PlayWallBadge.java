package de.tobias.playwall.client.view.components;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PlayWallBadge extends Button
{
	private final Label label;

	public PlayWallBadge(String labelText)
	{
		this.getStyleClass().add("badge");

		label = new Label(labelText);

		final FontIcon fontIcon = new FontIcon(FontAwesomeType.XMARK_SOLID);
		fontIcon.setSize(12);
		fontIcon.setMouseTransparent(true);
		setGraphic(fontIcon);

		final HBox box = new HBox(label, fontIcon);
		box.setSpacing(ViewConstants.DEFAULT_SPACING / 2);
		box.setAlignment(Pos.CENTER);
		this.setGraphic(box);
	}

	public void updateText(String labelText)
	{
		label.setText(labelText);
	}
}
