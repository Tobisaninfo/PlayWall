package de.tobias.playwall.client.domain.settings.view.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.settings.BaseSettingsViewController;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Base class for a page in the program settings dialog.
 */
public abstract class BaseProgramSettingsViewController extends BaseSettingsViewController<BaseProgramSettingsViewController.Param>
{
	@AllArgsConstructor
	@Getter
	public static class Param
	{
		protected Settings settings;
		protected List<AudioDeviceInstance> outputDevices;
	}

	@InjectConstructor
	protected BaseProgramSettingsViewController(FluentClient client)
	{
		super(client);
	}
}
