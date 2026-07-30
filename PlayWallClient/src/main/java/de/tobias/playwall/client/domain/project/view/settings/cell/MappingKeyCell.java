package de.tobias.playwall.client.domain.project.view.settings.cell;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class MappingKeyCell extends ListCell<InputKey>
{
	private final Mapping mapping;

	final VBox box;
	final Label keyLabel;
	final Label actionLabel;

	public MappingKeyCell(Mapping mapping)
	{
		this.mapping = mapping;

		box = new VBox(7);
		keyLabel = new Label();
		actionLabel = new Label();
		box.getChildren().addAll(keyLabel, actionLabel);
	}

	@Override
	protected void updateItem(InputKey item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(box);

		if(empty || item == null)
		{
			keyLabel.setText("");
			actionLabel.setText("");
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
		if(action != null)
		{
			actionLabel.setText(action.toString());
		}
		else
		{
			actionLabel.setText("<Leer>");
		}
	}
}
