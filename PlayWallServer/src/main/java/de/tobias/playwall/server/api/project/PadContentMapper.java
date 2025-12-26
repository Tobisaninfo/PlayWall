package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadContentDto;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.PadContent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.SubclassMapping;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PadContentMapper
{
	@SubclassMapping(target = AudioPadContentDto.class, source = AudioPadContent.class)
	PadContentDto padContentToPadContentDto(PadContent pad);
}
