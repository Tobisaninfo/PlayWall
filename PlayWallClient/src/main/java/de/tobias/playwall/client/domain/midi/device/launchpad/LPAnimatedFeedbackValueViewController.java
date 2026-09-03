package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueSettingsViewController;
import de.tobias.playwall.client.utils.Localizable;
import de.tobias.playwall.client.view.components.settings.SettingsRow;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping/feedback", view = "LPAnimatedFeedbackValueView", applyToStage = false)
class LPAnimatedFeedbackValueViewController extends FeedbackValueSettingsViewController
{
	@FXML
	private SettingsRow settingsRow;

	@FXML
	private CheckBox activeCheckbox;

	@Override
	public FeedbackValue createNewFeedback()
	{
		return new LPAnimatedFeedbackValue(true);
	}

	@Override
	public void initSettings(FeedbackState feedbackState, FeedbackValue feedbackValue)
	{
		if(!(feedbackValue instanceof LPAnimatedFeedbackValue lpAnimatedFeedbackValue))
		{
			throw new IllegalArgumentException("FeedbackValue must be of type LPAnimatedFeedbackValue");
		}
		if(feedbackState instanceof Localizable localizable)
		{
			settingsRow.setTitle(localizable.localize());
		}
		else
		{
			settingsRow.setTitle(feedbackState.toString());
		}

		activeCheckbox.setSelected(lpAnimatedFeedbackValue.isFlashing());
	}

	@Override
	public void applySettings(FeedbackValue feedbackValue)
	{
		if(!(feedbackValue instanceof LPAnimatedFeedbackValue lpAnimatedFeedbackValue))
		{
			throw new IllegalArgumentException("FeedbackValue must be of type LPFeedbackValue");
		}
		lpAnimatedFeedbackValue.setFlashing(activeCheckbox.isSelected());
	}
}
