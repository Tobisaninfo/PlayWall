package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.server.api.project.model.Pad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PadMetadataMapper
{
	PadDto padMetadataToPadMetadataDto(Pad pad);
}
