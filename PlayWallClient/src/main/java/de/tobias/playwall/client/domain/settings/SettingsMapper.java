package de.tobias.playwall.client.domain.settings;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.settings.model.SettingsDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class SettingsMapper
{
	public Settings settingsDtoToSettings(SettingsDto settingsDto)
	{
		return Settings.builder()
				.autoLoadLatestProjectOnStart(settingsDto.autoLoadLatestProjectOnStart())
				.unsavedChangesMode(settingsDto.unsavedChangesMode())
				.build();
	}

	public SettingsDto settingToSettingsDto(Settings settings)
	{
		return SettingsDto.builder()
				.autoLoadLatestProjectOnStart(settings.isAutoLoadLatestProjectOnStart())
				.unsavedChangesMode(settings.getUnsavedChangesMode())
				.build();
	}
}
