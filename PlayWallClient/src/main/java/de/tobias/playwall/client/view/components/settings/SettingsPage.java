package de.tobias.playwall.client.view.components.settings;

import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@DefaultProperty("items")
public class SettingsPage extends VBox
{
	protected final VBox settingsItemsVbox;

	public SettingsPage()
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, 0, ViewConstants.DEFAULT_SPACING));

		this.settingsItemsVbox = new VBox();
		this.settingsItemsVbox.setAlignment(Pos.TOP_LEFT);
		this.settingsItemsVbox.setSpacing(ViewConstants.DEFAULT_SPACING);
		this.settingsItemsVbox.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

		getChildren().addAll(settingsItemsVbox);

		VBox.setVgrow(this, Priority.ALWAYS);
	}

	public ObservableList<Node> getItems()
	{
		return settingsItemsVbox.getChildren();
	}
}