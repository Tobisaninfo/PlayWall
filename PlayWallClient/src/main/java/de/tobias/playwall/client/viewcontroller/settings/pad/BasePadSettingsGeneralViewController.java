package de.tobias.playwall.client.viewcontroller.settings.pad;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.viewcontroller.settings.BaseSettingsViewController;
import lombok.AllArgsConstructor;

public abstract class BasePadSettingsGeneralViewController extends BaseSettingsViewController<BasePadSettingsGeneralViewController.Param>
{
	@AllArgsConstructor
	public static class Param
	{
		protected Pad pad;
	}

	@InjectConstructor
	public BasePadSettingsGeneralViewController(FluentClient client)
	{
		super(client);
	}
}
