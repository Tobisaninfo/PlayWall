package de.tobias.playwall.client.domain.project.view.settings.mapping;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import de.tobias.playwall.client.view.ParamModalDialogBase;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractInputKeyDialog<T extends InputKey> extends ParamModalDialogBase<AbstractInputKeyDialog.Param, T>
{
	public record Param(Mapping mapping, boolean autoSubmit)
	{
	}

	@FXML
	protected VBox root;
	@FXML
	protected Label keyLabel;
	@FXML
	protected Label errorLabel;
	@FXML
	protected Button saveButton;
	@FXML
	protected Button cancelButton;

	@Getter(AccessLevel.NONE)
	protected Mapping mapping;

	@Getter(AccessLevel.NONE)
	protected boolean autoSubmit;

	protected void updateInputState(T key, Function<T, String> stringConverter)
	{
		keyLabel.getStyleClass().removeAll("key-input-placeholder", "key-input-label", "error-label");

		if(key == null || mapping == null)
		{
			keyLabel.setText(Localization.getString(Strings.UI_DIALOG_KEYBOARD_INPUT_PLACEHOLDER));
			keyLabel.getStyleClass().add("key-input-placeholder");
			return;
		}

		final boolean isAlreadyUsed = !autoSubmit && mapping.getAllInputKeys().contains(key);

		keyLabel.setText(stringConverter.apply(key));
		keyLabel.getStyleClass().add("key-input-label");
		if(isAlreadyUsed)
		{
			keyLabel.getStyleClass().add("error-label");
		}

		errorLabel.setVisible(isAlreadyUsed);
		saveButton.setDisable(isAlreadyUsed);
	}
}
