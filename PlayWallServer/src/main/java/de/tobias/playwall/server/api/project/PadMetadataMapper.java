package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.PadMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PadMetadataMapper
{
	PadMetadataDto padMetadataToPadMetadataDto(PadMetadata pad);
}
