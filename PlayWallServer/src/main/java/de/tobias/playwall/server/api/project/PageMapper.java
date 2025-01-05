package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.server.common.model.project.Page;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PadMapper.class})
public interface PageMapper
{
	PageDto pageToPageDto(Page page);
}
