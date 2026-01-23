package de.tobias.playwall.server.api.pad;

import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.server.common.model.pad.Pad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PadContentMapper.class})
public interface PadMapper
{
	PadDto padToPadDto(Pad pad);

	Pad padDtoToPad(PadDto pad);
}
