package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.server.api.project.model.Page;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PageMetadataMapper
{
	PageDto pageMetadataToPageMetadataDto(Page page);
}
