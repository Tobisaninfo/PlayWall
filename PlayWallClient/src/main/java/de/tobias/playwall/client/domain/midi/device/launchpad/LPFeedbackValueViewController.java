package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueSettingsViewController;
import de.tobias.playwall.client.view.components.ColorButton;
import de.tobias.playwall.client.view.components.ColorPicker;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping/feedback", view = "LPFeedbackValueView", applyToStage = false)
class LPFeedbackValueViewController extends FeedbackValueSettingsViewController
{
	@FXML
	private SettingsRow settingsRow;

	@FXML
	private ColorButton colorButton;
	private ColorPicker<LPColor> colorPicker;

	@Override
	public FeedbackValue createNewFeedback()
	{
		return new LPFeedbackValue(0);
	}

	@Override
	public void initSettings(FeedbackState feedbackState, FeedbackValue feedbackValue)
	{
		if(!(feedbackValue instanceof LPFeedbackValue lpFeedbackValue))
		{
			throw new IllegalArgumentException("FeedbackValue must be of type LPFeedbackValue");
		}
		settingsRow.setTitle(feedbackState.toString());

		final LPColor color = LPColor.fromMidiValue(lpFeedbackValue.getValue());

		colorPicker = new ColorPicker<>(color, LPColor.values(), colorButton::updateColor);
		colorButton.updateColor(color);
	}

	@Override
	public void applySettings(FeedbackValue feedbackValue)
	{
		if(!(feedbackValue instanceof LPFeedbackValue lpFeedbackValue))
		{
			throw new IllegalArgumentException("FeedbackValue must be of type LPFeedbackValue");
		}
		lpFeedbackValue.setValue(colorPicker.getSelectedColor().getMidiValue());
	}

	@FXML
	private void onColorButton(ActionEvent e)
	{
		colorPicker.hide();
		colorPicker.show(colorButton);
	}
}
