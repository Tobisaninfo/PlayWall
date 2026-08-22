package de.tobias.playwall.client.domain.midi.feedback;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackState;

@JsonTypeName("default-feedback-state")
public enum DefaultFeedbackState implements FeedbackState
{
	NORMAL,
	ACTIVE;

	@Override
	public FeedbackState copy()
	{
		return this;
	}
}
