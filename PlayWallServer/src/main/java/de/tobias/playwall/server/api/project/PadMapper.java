package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.server.common.model.project.Pad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PadContentMapper.class})
public interface PadMapper
{
	PadDto padToPadDto(Pad pad);
}
