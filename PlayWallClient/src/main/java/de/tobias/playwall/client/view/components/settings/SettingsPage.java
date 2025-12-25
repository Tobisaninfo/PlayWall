package de.tobias.playwall.client.view.components.settings;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.application.Platform;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@DefaultProperty("items")
public class SettingsPage extends VBox
{
	private final VBox settingsItemsVbox;
	private final PlayWallButton cancelButton;
	private final PlayWallButton saveButton;

	private final ObjectProperty<EventHandler<ActionEvent>> onSave = new SimpleObjectProperty<>();
	private final ObjectProperty<EventHandler<ActionEvent>> onCancel = new SimpleObjectProperty<>();

	private final StringProperty saveText = new SimpleStringProperty();
	private final StringProperty cancelText = new SimpleStringProperty();

	public SettingsPage()
	{
		setAlignment(Pos.TOP_LEFT);
		setSpacing(ViewConstants.DEFAULT_SPACING);
		setPadding(new Insets(ViewConstants.DEFAULT_SPACING, 0, 0, ViewConstants.DEFAULT_SPACING));

		this.settingsItemsVbox = new VBox();
		this.settingsItemsVbox.setAlignment(Pos.TOP_LEFT);
		this.settingsItemsVbox.setSpacing(ViewConstants.DEFAULT_SPACING);
		this.settingsItemsVbox.setPadding(new Insets(0, ViewConstants.DEFAULT_SPACING, 0, ViewConstants.DEFAULT_SPACING));

		final Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		this.saveButton = new PlayWallButton("", FontAwesomeType.FLOPPY_DISK_SOLID);
		this.saveButton.setDefaultButton(true);
		this.saveButton.textProperty().bind(saveText);
		this.saveButton.onActionProperty().bind(onSave);

		this.cancelButton = new PlayWallButton("", FontAwesomeType.XMARK_SOLID);
		this.cancelButton.textProperty().bind(cancelText);
		this.cancelButton.onActionProperty().bind(onCancel);

		final HBox boxButtons = new HBox(cancelButton, saveButton);
		boxButtons.setSpacing(ViewConstants.DEFAULT_SPACING);
		boxButtons.setAlignment(Pos.CENTER_RIGHT);

		getChildren().addAll(settingsItemsVbox, spacer, boxButtons);

		VBox.setVgrow(this, Priority.ALWAYS);

		Platform.runLater(this.saveButton::requestFocus);
	}

	public ObservableList<Node> getItems()
	{
		return settingsItemsVbox.getChildren();
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
		this.saveButton.setText(text);
	}

	public void disableButtonSave(boolean disable)
	{
		this.saveButton.setDisable(disable);
	}

	public void setOnCancelAction(EventHandler<ActionEvent> handler)
	{
		this.cancelButton.setOnAction(handler);
	}

	public void setOnSaveAction(EventHandler<ActionEvent> handler)
	{
		this.saveButton.setOnAction(handler);
	}

	public String getSaveText()
	{
		return saveText.get();
	}

	public StringProperty saveTextProperty()
	{
		return saveText;
	}

	public void setSaveText(String saveText)
	{
		this.saveText.set(saveText);
	}

	public String getCancelText()
	{
		return cancelText.get();
	}

	public StringProperty cancelTextProperty()
	{
		return cancelText;
	}

	public void setCancelText(String cancelText)
	{
		this.cancelText.set(cancelText);
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}
}
