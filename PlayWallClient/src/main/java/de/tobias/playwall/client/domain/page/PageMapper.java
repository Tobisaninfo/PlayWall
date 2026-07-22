package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.page.PageDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PageMapper
{
	private final PadMapper padMapper;
	private final PageSettingsMapper pageSettingsMapper;

	public Page pageDtoToPage(PageDto page)
	{
		final List<Pad> pads = new ArrayList<>();
		for(PadDto padDto : page.pads())
		{
			pads.add(padMapper.padDtoToPad(padDto));
		}

		return new Page(page.id(), pageSettingsMapper.pageSettingsDtoToPageSettings(page.settings()), page.position(), pads);
	}
}
