package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.FadeSettingsDto;
import de.tobias.playwall.server.common.model.project.FadeSettings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FadeSettingsMapper
{
	FadeSettings fadeSettingsDtoToFadeSettings(FadeSettingsDto fadeSettingsDto);
}
