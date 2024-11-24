package de.tobias.playwall.common.api.project.model;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PageDto(UUID id, String name, Integer position, List<PadDto> pads)
{
}
