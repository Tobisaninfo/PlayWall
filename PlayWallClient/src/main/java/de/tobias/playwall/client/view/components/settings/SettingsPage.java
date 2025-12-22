package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

@DefaultProperty("items")
public class SettingsPage extends VBox
{
	private final VBox vboxSettingsItems;
	private final PlayWallButton buttonCancel;
	private final PlayWallButton buttonSave;

	private final ObjectProperty<EventHandler<ActionEvent>> onSave = new SimpleObjectProperty<>();
	private final ObjectProperty<EventHandler<ActionEvent>> onCancel = new SimpleObjectProperty<>();

	public SettingsPage()
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, 0, ViewConstants.DEFAULT_SPACING));

		this.vboxSettingsItems = new VBox();
		this.vboxSettingsItems.setAlignment(Pos.TOP_LEFT);
		this.vboxSettingsItems.setSpacing(ViewConstants.DEFAULT_SPACING);
		this.vboxSettingsItems.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

		final Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		this.buttonCancel = new PlayWallButton("", FontAwesomeType.XMARK_SOLID);
		this.buttonSave = new PlayWallButton("", FontAwesomeType.FLOPPY_DISK_SOLID);

		this.buttonSave.onActionProperty().bind(onSave);
		this.buttonCancel.onActionProperty().bind(onCancel);

		final HBox boxButtons = new HBox(buttonCancel, buttonSave);
		boxButtons.setSpacing(ViewConstants.DEFAULT_SPACING);
		boxButtons.setAlignment(Pos.CENTER_RIGHT);

		getChildren().addAll(vboxSettingsItems, spacer, boxButtons);

		VBox.setVgrow(this, Priority.ALWAYS);

		Platform.runLater(this.buttonSave::requestFocus);
	}

	@SuppressWarnings("unused")
	public void setResources(ResourceBundle resources)
	{
		if(resources != null)
		{
			this.buttonCancel.setText(resources.getString("ui.settings.button.cancel"));
			this.buttonSave.setText(resources.getString("ui.settings.button.save"));
		}
	}

	public ObservableList<Node> getItems()
	{
		return vboxSettingsItems.getChildren();
	}

	public EventHandler<ActionEvent> getOnSave()
	{
		return onSave.get();
	}

	public void setOnSave(EventHandler<ActionEvent> handler)
	{
		this.onSave.set(handler);
	}

	public ObjectProperty<EventHandler<ActionEvent>> onSaveProperty()
	{
		return onSave;
	}

	public EventHandler<ActionEvent> getOnCancel()
	{
		return onCancel.get();
	}

	public void setOnCancel(EventHandler<ActionEvent> handler)
	{
		this.onCancel.set(handler);
	}

	public ObjectProperty<EventHandler<ActionEvent>> onCancelProperty()
	{
		return onCancel;
	}

	public void setButtonSaveText(String text)
	{
		this.buttonSave.setText(text);
	}

	public void disableButtonSave(boolean disable)
	{
		this.buttonSave.setDisable(disable);
	}

	public void setOnCancelAction(EventHandler<ActionEvent> handler)
	{
		this.buttonCancel.setOnAction(handler);
	}

	public void setOnSaveAction(EventHandler<ActionEvent> handler)
	{
		this.buttonSave.setOnAction(handler);
	}
}
