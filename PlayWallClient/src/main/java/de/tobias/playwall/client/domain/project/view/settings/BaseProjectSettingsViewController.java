package de.tobias.playwall.client.domain.project.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for a page in the project settings dialog.
 */
public abstract class BaseProjectSettingsViewController extends BaseSettingsViewController<BaseProjectSettingsViewController.Param>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		protected ProjectMetadata projectMetadata;
		protected Class<? extends BaseProjectSettingsViewController> openTab;
	}

	@InjectConstructor
	protected BaseProjectSettingsViewController(FluentClient client)
	{
		super(client);
	}
}
