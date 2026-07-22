package de.tobias.playwall.common.api.page;

import de.tobias.playwall.common.api.pad.PadDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PageDto(UUID id, PageSettingsDto settings, Integer position, List<PadDto> pads)
{
}
