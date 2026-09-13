package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@JsonTypeName("globalVolume")
@ActionDescription(nameKey = "action.global.volume.name", order = 4, feedbackTypes = DefaultFeedbackState.NORMAL, settingsViewController = GlobalVolumeActionSettingsViewController.class)
@Getter
@Setter
@AllArgsConstructor
public class GlobalVolumeAction implements Action
{
	@RequiredArgsConstructor
	@Getter
	public enum VolumeChangeMode
	{
		DECREASE("-"),
		INCREASE("+");

		private final String symbol;
	}

	@RequiredArgsConstructor
	@Getter
	public enum VolumeChangeDelta
	{
		FIVE(0.05),
		TEN(0.10);

		private final Double delta;
	}

	private VolumeChangeMode volumeChangeMode;

	private VolumeChangeDelta delta;

	@Override
	public Action copy()
	{
		return new GlobalVolumeAction(volumeChangeMode, delta);
	}

	@Override
	public String toString()
	{
		return Localization.getString("action.global.volume.details", volumeChangeMode.getSymbol(), delta.getDelta() * 100);
	}
}
