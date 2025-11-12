package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.di.Component;
import de.tobias.playwall.client.di.InjectConstructor;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.common.api.project.model.PageDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class PageMapper
{
	private final PadMapper padMapper;

	public Page pageDtoToPage(PageDto page)
	{
		final List<Pad> pads = new ArrayList<>();
		for(PadDto padDto : page.pads())
		{
			pads.add(padMapper.padDtoToPad(padDto));
		}

		return new Page(page.id(), page.name(), page.position(), pads);
	}
}
