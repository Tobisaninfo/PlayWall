package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueSettingsViewController;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping/feedback", view = "LPFeedbackValueView", applyToStage = false)
class LPFeedbackValueViewController extends FeedbackValueSettingsViewController
{
	@Override
	public FeedbackValue createNewFeedback()
	{
		return new LPFeedbackValue(0);
	}

	@Override
	public void initSettings(FeedbackValue feedbackValue)
	{

	}

	@Override
	public void applySettings(FeedbackValue feedbackValue)
	{

	}
}
