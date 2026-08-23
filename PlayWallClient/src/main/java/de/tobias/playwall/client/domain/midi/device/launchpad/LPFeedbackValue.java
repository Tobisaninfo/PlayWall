package de.tobias.playwall.client.domain.midi.device.launchpad;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@JsonTypeName("lp-feedback-value")
@FeedbackValueDescription(settingsViewController = LPFeedbackValueViewController.class)
public class LPFeedbackValue implements FeedbackValue
{
	@Getter
	@Setter
	private int value;

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

	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (LPFeedbackValue) obj;
		return this.value == that.value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(value);
	}

	@Override
	public String toString()
	{
		return "LPFeedbackValue[" +
			   "value=" + value + ']';
	}

}
