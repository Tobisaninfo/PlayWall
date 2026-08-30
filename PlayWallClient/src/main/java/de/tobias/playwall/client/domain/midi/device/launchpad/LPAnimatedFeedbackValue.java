package de.tobias.playwall.client.domain.midi.device.launchpad;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.tobias.playwall.client.domain.midi.feedback.FeedbackValueDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@JsonTypeName("lp-animated-feedback-value")
@FeedbackValueDescription(settingsViewController = LPAnimatedFeedbackValueViewController.class)
public class LPAnimatedFeedbackValue implements FeedbackValue
{
	@Getter
	@Setter
	private boolean flashing;

	@JsonCreator
	public LPAnimatedFeedbackValue(@JsonProperty("flashing") boolean flashing)
	{
		this.flashing = flashing;
	}

	@Override
	public FeedbackValue copy()
	{
		return new LPAnimatedFeedbackValue(flashing);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (LPAnimatedFeedbackValue) obj;
		return this.flashing == that.flashing;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(flashing);
	}

	@Override
	public String toString()
	{
		return "LPAnimatedFeedbackValue[" +
			   "flashing=" + flashing + ']';
	}

}
