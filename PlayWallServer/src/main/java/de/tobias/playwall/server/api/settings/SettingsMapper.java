package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.common.api.settings.model.SettingsDto;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.common.model.settings.Settings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SettingsMapper
{
	SettingsDto settingsToSettingsDto(Settings settings);

	Settings settingsDtoToSettings(SettingsDto settings);
}
