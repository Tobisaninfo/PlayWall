package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.ui.NVC;

/**
 * Abstract base view controller for the settings view of an action of the mapping. It contains customization for the action.
 */
public abstract class ActionSettingsViewController extends NVC
{
	/**
	 * Create a new instance of the action with default settings. If possible, no uninitialized values.
	 *
	 * @return new action
	 */
	public abstract Action createNewAction();

	/**
	 * Set the action settings values to the view components
	 *
	 * @param action action to set the settings for
	 */
	public abstract void initSettings(Action action);

	/**
	 * Apply the settings from the view components to the action to persist
	 *
	 * @param action action to apply the settings to
	 */
	public abstract void applySettings(Action action);
}
