package de.tobias.playwall.client.domain.midi.feedback;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;

@JsonTypeName("default-feedback-value")
public record DefaultFeedbackValue(int value) implements FeedbackValue
{
	@JsonCreator
	public DefaultFeedbackValue(@JsonProperty("value") int value)
	{
		this.value = value;
	}

	@Override
	public FeedbackValue copy()
	{
		return new DefaultFeedbackValue(value);
	}
}
