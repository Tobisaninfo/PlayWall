package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.AudioPadDto;
import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.server.common.model.project.AudioPad;
import de.tobias.playwall.server.common.model.project.Pad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PadMetadataMapper
{
	@SubclassMapping(target = AudioPadDto.class, source = AudioPad.class)
	PadDto padMetadataToPadMetadataDto(Pad pad);
}
