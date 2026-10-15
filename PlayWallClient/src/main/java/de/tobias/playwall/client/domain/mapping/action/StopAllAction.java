package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeName("stopAll")
@ActionDescription(nameKey = "action.stop.all.name", feedbackTypes = DefaultFeedbackState.NORMAL, order = 3, settingsViewController = StopAllActionSettingsViewController.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StopAllAction implements Action
{
	private boolean shouldFadeOut;

	@Override
	public Action copy()
	{
		return new StopAllAction(shouldFadeOut);
	}

	@Override
	public String toString()
	{
		return Localization.getString("action.stop.all.name");
	}
}
