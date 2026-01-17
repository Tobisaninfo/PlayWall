package de.tobias.playwall.client.domain.pad.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for a page in the pad settings dialog.
 */
public abstract class BasePadSettingsViewController extends BaseSettingsViewController<BasePadSettingsViewController.Param>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		protected Pad pad;
		protected PadSettingsViewController parentDialog;
	}

	@InjectConstructor
	public BasePadSettingsViewController(FluentClient client)
	{
		super(client);
	}
}
