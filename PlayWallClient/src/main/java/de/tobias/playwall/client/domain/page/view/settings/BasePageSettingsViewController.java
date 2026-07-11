package de.tobias.playwall.client.domain.page.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for a page in the page settings dialog.
 */
public abstract class BasePageSettingsViewController extends BaseSettingsViewController<BasePageSettingsViewController.Param>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		protected Page page;
	}

	@InjectConstructor
	protected BasePageSettingsViewController(FluentClient client)
	{
		super(client);
	}
}
