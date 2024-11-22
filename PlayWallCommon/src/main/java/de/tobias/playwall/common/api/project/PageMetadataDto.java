package de.tobias.playwall.common.api.project;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PageMetadataDto(UUID id, String name, Integer position, List<PadMetadataDto> pads)
{
}
