package de.tobias.playwall.client.domain.mapping.action;

import de.thecodelabs.midi.mapping.action.Action;
import de.tobias.playwall.client.appcontext.ViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

@ViewController(path = "de/tobias/playwall/client/view/settings/project/mapping", view = "StopAllActionSettingsView", applyToStage = false)
class StopAllActionSettingsViewController extends ActionSettingsViewController
{
	@FXML
	private CheckBox shouldFadeOutCheckBox;

	@Override
	public Action createNewAction()
	{
		return new StopAllAction(true);
	}

	@Override
	public void initSettings(Action action)
	{
		if(action instanceof StopAllAction stopAllAction)
		{
			shouldFadeOutCheckBox.setSelected((stopAllAction).isShouldFadeOut());
		}
	}

	@Override
	public void applySettings(Action action)
	{
		if(action instanceof StopAllAction stopAllAction)
		{
			stopAllAction.setShouldFadeOut(shouldFadeOutCheckBox.isSelected());
		}
	}
}
