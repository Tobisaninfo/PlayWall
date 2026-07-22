package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.project.ColorMapper;
import de.tobias.playwall.common.api.page.PageSettingsDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PageSettingsMapper
{
	private final ColorMapper colorMapper;

	public PageSettings pageSettingsDtoToPageSettings(PageSettingsDto dto)
	{
		return new PageSettings(dto.name(), colorMapper.colorToModernColor(dto.color()));
	}

	public PageSettingsDto pageSettingsToPageSettingsDto(PageSettings settings)
	{
		return new PageSettingsDto(settings.getName(), colorMapper.modernColorToColor(settings.getColor()));
	}
}
