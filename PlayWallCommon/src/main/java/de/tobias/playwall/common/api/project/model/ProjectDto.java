package de.tobias.playwall.common.api.project.model;

import lombok.Builder;

import java.util.List;

@Builder
public record ProjectDto(ProjectMetadataDto metadata, List<PageDto> pages)
{
}
