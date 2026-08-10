package de.tobias.playwall.client.domain.project.view;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import de.tobias.playwall.client.view.ParamModalDialogBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@ViewController(path = "de/tobias/playwall/client/view/dialog", view = "KeyboardInputDialog")
public class KeyboardInputDialog extends ParamModalDialogBase<KeyboardInputDialog.Param, KeyboardInputKey>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		private final Mapping mapping;
		private final boolean autoSubmit;
	}

	private static final Set<KeyCode> RESERVED_KEYS = Set.of(KeyCode.ENTER, KeyCode.SPACE, KeyCode.ESCAPE, KeyCode.NUM_LOCK);

	@FXML
	private VBox root;
	@FXML
	private Label keyLabel;
	@FXML
	private Label errorLabel;
	@FXML
	private Button saveButton;

	@Getter(AccessLevel.NONE)
	private Mapping mapping;

	@Getter(AccessLevel.NONE)
	private boolean autoSubmit;

	@Getter(AccessLevel.NONE)
	private KeyboardInputKey selectedKey;

	@Override
	protected void init()
	{
		root.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		updateInputState(null);
	}

	@Override
	public void initParameter(Param parameter)
	{
		this.mapping = parameter.getMapping();
		this.autoSubmit = parameter.isAutoSubmit();
		saveButton.setVisible(!autoSubmit);
		saveButton.setManaged(!autoSubmit);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stage.setTitle(Localization.getString(Strings.UI_DIALOG_KEYBOARD_INPUT_TITLE));
		stage.setResizable(false);

		stage.setHeight(210);
		stage.setWidth(350);
	}

	@Override
	protected KeyboardInputKey getResultValue()
	{
		return selectedKey;
	}

	@Override
	public void onCloseRequest()
	{
		selectedKey = null;
	}

	// Event Handler

	@FXML
	private void submitHandler(ActionEvent event)
	{
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void cancelHandler(ActionEvent event)
	{
		selectedKey = null;
		getStageContainer().ifPresent(NVCStage::close);
	}

	private void onKeyPressed(KeyEvent event)
	{
		final KeyCode code = event.getCode();
		if(code == KeyCode.SPACE)
		{
			event.consume();
			return;
		}
		if(code.isModifierKey() || code == KeyCode.UNDEFINED || RESERVED_KEYS.contains(code))
		{
			return;
		}

		final KeyboardInputKey key = new KeyboardInputKey(code, code.getName());
		if(mapping != null)
		{
			selectedKey = key;
			updateInputState(key);
			if(autoSubmit)
			{
				getStageContainer().ifPresent(NVCStage::close);
			}
		}
	}

	private void updateInputState(KeyboardInputKey key)
	{
		keyLabel.getStyleClass().removeAll("key-input-placeholder", "key-input-label", "error-label");

		if(key == null || mapping == null)
		{
			keyLabel.setText(Localization.getString(Strings.UI_DIALOG_KEYBOARD_INPUT_PLACEHOLDER));
			keyLabel.getStyleClass().add("key-input-placeholder");
			return;
		}

		final boolean isAlreadyUsed = !autoSubmit && mapping.getAllInputKeys().contains(key);

		keyLabel.setText(KeyNameLocalizer.getKeyName(key.code()));
		keyLabel.getStyleClass().add("key-input-label");
		if(isAlreadyUsed)
		{
			keyLabel.getStyleClass().add("error-label");
		}

		errorLabel.setVisible(isAlreadyUsed);
		saveButton.setDisable(isAlreadyUsed);
	}
}
