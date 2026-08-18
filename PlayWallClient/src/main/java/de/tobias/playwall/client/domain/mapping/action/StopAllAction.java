package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;

@JsonTypeName("stopAll")
@ActionDescription(nameKey = "action.stop.all.name", order = 3, settingsViewController = StopAllActionSettingsViewController.class)
public class StopAllAction implements Action
{
	@Override
	public Action copy()
	{
		return new StopAllAction();
	}

	@Override
	public String toString()
	{
		return Localization.getString("action.stop.all.name");
	}
}
