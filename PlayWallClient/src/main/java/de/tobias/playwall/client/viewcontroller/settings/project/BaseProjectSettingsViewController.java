package de.tobias.playwall.client.viewcontroller.settings.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.viewcontroller.settings.BaseSettingsViewController;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for a page in the pad settings dialog.
 */
public abstract class BaseProjectSettingsViewController extends BaseSettingsViewController<BaseProjectSettingsViewController.Param>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		protected ProjectMetadata projectMetadata;
	}

	@InjectConstructor
	public BaseProjectSettingsViewController(FluentClient client)
	{
		super(client);
	}
}
