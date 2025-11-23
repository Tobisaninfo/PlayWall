package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class SettingsEntry extends HBox
{
	private boolean hasContent = false;

	public SettingsEntry(FontIconType iconType, String labelText, double labelMinWidth)
	{
		final Label labelName = new Label();
		labelName.setText(labelText);
		labelName.setMinWidth(labelMinWidth);
		labelName.setPrefWidth(labelMinWidth);
		labelName.getStyleClass().add("settings-entry-label");

		final FontIcon icon = new FontIcon(iconType);
		icon.setMouseTransparent(true);
		icon.setSize(16);

		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);

		getChildren().addAll(icon, labelName);
	}

	public void setContent(Region content)
	{
		if(this.hasContent)
		{
			this.getChildren().removeLast();
		}

		this.getChildren().add(content);
		this.hasContent = true;
	}
}
