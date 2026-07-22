package de.tobias.playwall.server.api.page;

import de.tobias.playwall.common.api.page.PageSettingsDto;
import de.tobias.playwall.server.common.model.page.PageSettings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PageSettingsMapper
{
	PageSettingsDto pageSettingsToPageSettingsDto(PageSettings pageSettings);

	PageSettings pageSettingsDtoToPageSettings(PageSettingsDto pageSettingsDto);
}
