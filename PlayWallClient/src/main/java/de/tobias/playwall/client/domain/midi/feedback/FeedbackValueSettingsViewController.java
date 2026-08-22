package de.tobias.playwall.client.domain.midi.feedback;

import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.utils.ui.NVC;

/**
 * Abstract base view controller for the settings view of a FeedbackValue of the mapping. It contains customization for the feedback value.
 */
public abstract class FeedbackValueSettingsViewController extends NVC
{
	/**
	 * Create a new instance of the feedback value with default settings. If possible, no uninitialized values.
	 *
	 * @return new feedback value
	 */
	public abstract FeedbackValue createNewFeedback();

	/**
	 * Set the action settings values to the view components
	 *
	 * @param action action to set the settings for
	 */
	public abstract void initSettings(FeedbackValue feedbackValue);

	/**
	 * Apply the settings from the view components to the FeedbackValue to persist
	 *
	 * @param feedbackValue feedbackValue to apply the settings to
	 */
	public abstract void applySettings(FeedbackValue feedbackValue);
}
