package de.tobias.playwall.common.api.project.model;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ProjectDto(UUID id, String name, int numberOfHorizontalPads, int numberOfVerticalPads, List<PageDto> pages)
{
}
