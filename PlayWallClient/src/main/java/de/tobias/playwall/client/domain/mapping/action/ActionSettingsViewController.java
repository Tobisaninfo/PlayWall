package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.ui.NVC;

public abstract class ActionSettingsViewController extends NVC
{
	public abstract Action createNewAction();

	public abstract void initAction(Action action);

	public abstract void saveAction(Action action);
}
