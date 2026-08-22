package de.tobias.playwall.client.domain.midi.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;

@JsonTypeName("default-feedback-value")
public record LPFeedbackValue(int value) implements FeedbackValue
{
	@JsonCreator
	public LPFeedbackValue(@JsonProperty("value") int value)
	{
		this.value = value;
	}

	@Override
	public FeedbackValue copy()
	{
		return new LPFeedbackValue(value);
	}
}
