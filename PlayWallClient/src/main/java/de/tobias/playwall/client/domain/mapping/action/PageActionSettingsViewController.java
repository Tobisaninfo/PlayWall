package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.tobias.playwall.client.appcontext.ViewController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "PageActionSettingsView", applyToStage = false)
class PageActionSettingsViewController extends ActionSettingsViewController
{
	@Override
	public Action createNewAction()
	{
		return new PageAction();
	}

	@Override
	public void setAction(Action action)
	{
		log.debug("Set action: {}", action);
	}
}
