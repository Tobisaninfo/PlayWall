package de.tobias.playwall.client.view.components.settings;

import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@DefaultProperty("items")
public class SettingsPage extends VBox
{
	protected final VBox settingsItemsVbox;
	private final ScrollPane scrollPane;

	private final BooleanProperty provideScrollPane = new SimpleBooleanProperty(true);

	public SettingsPage()
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, 0, ViewConstants.DEFAULT_SPACING));


		scrollPane = new ScrollPane();
		scrollPane.setFitToWidth(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setPadding(new Insets(0, 28, 0, 0));

		this.settingsItemsVbox = new VBox();
		this.settingsItemsVbox.setAlignment(Pos.TOP_LEFT);
		this.settingsItemsVbox.setSpacing(ViewConstants.DEFAULT_SPACING);
		this.settingsItemsVbox.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

		setChild();
		provideScrollPane.addListener((_, _, _) -> setChild());

		VBox.setVgrow(this, Priority.ALWAYS);
	}

	private void setChild()
	{
		getChildren().removeAll(scrollPane, settingsItemsVbox);
		if(provideScrollPane.get())
		{
			getChildren().addAll(scrollPane);
			scrollPane.setContent(settingsItemsVbox);
		}
		else
		{
			getChildren().addAll(settingsItemsVbox);
			VBox.setVgrow(settingsItemsVbox, Priority.ALWAYS);
		}
	}

	public ObservableList<Node> getItems()
	{
		return settingsItemsVbox.getChildren();
	}

	public boolean getProvideScrollPane()
	{
		return provideScrollPane.get();
	}

	public BooleanProperty provideScrollPaneProperty()
	{
		return provideScrollPane;
	}

	public void setProvideScrollPane(boolean provideScrollPane)
	{
		this.provideScrollPane.set(provideScrollPane);
	}
}