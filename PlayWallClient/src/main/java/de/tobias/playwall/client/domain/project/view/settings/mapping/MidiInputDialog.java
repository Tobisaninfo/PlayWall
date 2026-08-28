package de.tobias.playwall.client.domain.project.view.settings.mapping;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.message.MidiMessageListener;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.ParamModalDialogBase;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ViewController(path = "de/tobias/playwall/client/view/dialog", view = "InputKeyDialog")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class MidiInputDialog extends ParamModalDialogBase<MidiInputDialog.Param, MidiInputKey>
{
	public record Param(Mapping mapping, boolean autoSubmit)
	{
	}

	@FXML
	private VBox root;
	@FXML
	private Label keyLabel;
	@FXML
	private Label errorLabel;
	@FXML
	private Button saveButton;
	@FXML
	private Button cancelButton;

	private final Midi midi;
	private MidiMessageListener midiListener;

	@Getter(AccessLevel.NONE)
	private Mapping mapping;

	@Getter(AccessLevel.NONE)
	private boolean autoSubmit;

	@Getter(AccessLevel.NONE)
	private MidiInputKey selectedKey;

	@Override
	protected void init()
	{
		updateInputState(null);
		midiListener = midiMessage -> {
			midiMessage.consume();
			selectedKey = new MidiInputKey(midiMessage.getPayload()[0]);

			Platform.runLater(() -> {
				updateInputState(selectedKey);
				if(autoSubmit)
				{
					getStageContainer().ifPresent(NVCStage::close);
				}
			});
		};
	}

	@Override
	public void initParameter(Param parameter)
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

		stage.setOnShowing(_ -> midi.getDevice().getPublisher().addMidiListener(midiListener, 1));
		stage.setOnHiding(_ -> midi.getDevice().getPublisher().removeMidiListener(midiListener));
	}

	@Override
	protected MidiInputKey getResultValue()
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

	private void updateInputState(MidiInputKey key)
	{
		keyLabel.getStyleClass().removeAll("key-input-placeholder", "key-input-label", "error-label");

		if(key == null || mapping == null)
		{
			keyLabel.setText(Localization.getString(Strings.UI_DIALOG_KEYBOARD_INPUT_PLACEHOLDER));
			keyLabel.getStyleClass().add("key-input-placeholder");
			return;
		}

		final boolean isAlreadyUsed = !autoSubmit && mapping.getAllInputKeys().contains(key);

		keyLabel.setText(String.valueOf(key.value()));
		keyLabel.getStyleClass().add("key-input-label");
		if(isAlreadyUsed)
		{
			keyLabel.getStyleClass().add("error-label");
		}

		errorLabel.setVisible(isAlreadyUsed);
		saveButton.setDisable(isAlreadyUsed);
	}
}
