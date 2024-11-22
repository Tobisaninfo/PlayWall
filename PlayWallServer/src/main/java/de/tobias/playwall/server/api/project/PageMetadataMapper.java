package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.PageMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PageMetadataMapper
{
	PageMetadataDto pageMetadataToPageMetadataDto(PageMetadata page);
}
