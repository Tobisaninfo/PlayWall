package de.tobias.playwall.server.api.page;

import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.page.Page;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PadMapper.class})
public interface PageMapper
{
	PageDto pageToPageDto(Page page);
}
