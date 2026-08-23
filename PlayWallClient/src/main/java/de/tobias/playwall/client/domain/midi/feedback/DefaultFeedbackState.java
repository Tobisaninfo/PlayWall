package de.tobias.playwall.client.domain.midi.feedback;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.utils.Localizable;

@JsonTypeName("default-feedback-state")
public enum DefaultFeedbackState implements FeedbackState, Localizable
{
	NORMAL,
	ACTIVE;

	@Override
	public FeedbackState copy()
	{
		return this;
	}

	@Override
	public String localize()
	{
		return Localization.getString("DefaultFeedbackState." + name());
	}
}
