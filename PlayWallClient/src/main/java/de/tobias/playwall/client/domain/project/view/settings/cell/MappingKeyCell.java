package de.tobias.playwall.client.domain.project.view.settings.cell;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.view.components.PlayWallButton;
import de.tobias.playwall.client.view.components.ViewConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class MappingKeyCell extends ListCell<InputKey>
{
	private final Mapping mapping;

	final HBox hbox;
	final VBox vbox;
	final Label keyLabel;
	final Label actionLabel;
	final PlayWallButton buttonDelete;
	final Consumer<InputKey> deleteActionHandler;

	public MappingKeyCell(Mapping mapping, Consumer<InputKey> deleteActionHandler)
	{
		this.mapping = mapping;
		this.deleteActionHandler = deleteActionHandler;

		hbox = new HBox(ViewConstants.DEFAULT_SPACING);
		vbox = new VBox(ViewConstants.DEFAULT_SPACING / 2);
		keyLabel = new Label();
		actionLabel = new Label();

		vbox.getChildren().addAll(keyLabel, actionLabel);

		buttonDelete = new PlayWallButton(FontAwesomeType.TRASH_CAN_SOLID);

		hbox.getChildren().addAll(vbox, buttonDelete);
		hbox.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(vbox, Priority.ALWAYS);
	}

	@Override
	protected void updateItem(InputKey item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(hbox);

		if(empty || item == null)
		{
			keyLabel.setText("");
			actionLabel.setText("");
			buttonDelete.setVisible(false);
			return;
		}

		switch(item)
		{
			case KeyboardInputKey key ->
					keyLabel.setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_KEY_KEYBOARD, key.key()));
			case MidiInputKey midi ->
					keyLabel.setText(Localization.getString(Strings.UI_SETTINGS_PROJECT_MAPPING_KEY_MIDI, midi.value()));
			default -> throw new IllegalStateException("Unexpected value: " + item);
		}

		final Action action = mapping.getAction(item);
		if(action == null)
		{
			actionLabel.setText("<Leer>");
		}
		else
		{
			actionLabel.setText(action.toString());
		}

		buttonDelete.setVisible(true);
		buttonDelete.setOnAction(_ -> deleteActionHandler.accept(item));
	}
}
