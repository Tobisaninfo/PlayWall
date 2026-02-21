package de.tobias.playwall.client.domain.settings.view.main;

import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.domain.settings.SettingsMapper;
import de.tobias.playwall.client.event.EventListener;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SettingsListener
{
	private final SettingsMapper settingsMapper;
	private final ClientSettingsController settingsController;

	@EventListener(SettingsUpdate.class)
	void onSettingsUpdate(SettingsUpdate message)
	{
		final Settings settings = settingsMapper.settingsDtoToSettings(message.getSettings());
		settingsController.setSettings(settings);
	}
}
