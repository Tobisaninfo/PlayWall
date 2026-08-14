package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.tobias.playwall.client.appcontext.ViewController;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "StopAllActionSettingsView", applyToStage = false)
class StopAllActionSettingsViewController extends ActionSettingsViewController
{
	@Override
	public Action createNewAction()
	{
		return new StopAllAction();
	}

	@Override
	public void initSettings(Action action)
	{
		// Nothing to do
	}

	@Override
	public void applySettings(Action action)
	{
		// Nothing to do
	}
}
