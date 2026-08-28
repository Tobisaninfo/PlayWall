package de.tobias.playwall.client.domain.project.view.settings.mapping;

import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.mapping.KeyNameLocalizer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Set;

@ViewController(path = "de/tobias/playwall/client/view/dialog", view = "InputKeyDialog")
public class KeyboardInputDialog extends AbstractInputKeyDialog<KeyboardInputKey>
{
	private static final Set<KeyCode> RESERVED_KEYS = Set.of(KeyCode.ENTER, KeyCode.SPACE, KeyCode.ESCAPE, KeyCode.NUM_LOCK);

	@Getter(AccessLevel.NONE)
	protected KeyboardInputKey selectedKey;

	@Override
	protected void init()
	{
		updateInputState(null, this::keyToString);
	}

	@Override
	public void initParameter(AbstractInputKeyDialog.Param parameter)
	{
		this.mapping = parameter.mapping();
		this.autoSubmit = parameter.autoSubmit();
		cancelButton.setFocusTraversable(false);
		saveButton.setFocusTraversable(false);
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

		stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
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
			event.consume();
			updateInputState(key, this::keyToString);
			if(autoSubmit)
			{
				getStageContainer().ifPresent(NVCStage::close);
			}
		}
	}

	private String keyToString(KeyboardInputKey key)
	{
		return KeyNameLocalizer.getKeyName(key.code());
	}
}
