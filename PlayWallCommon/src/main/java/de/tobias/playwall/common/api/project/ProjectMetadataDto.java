package de.tobias.playwall.common.api.project;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProjectMetadataDto(UUID id, String name, List<PageMetadataDto> pages)
{
}
