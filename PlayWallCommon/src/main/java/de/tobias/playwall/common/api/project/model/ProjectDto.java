package de.tobias.playwall.common.api.project.model;

import de.tobias.playwall.common.api.page.PageDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ProjectDto(ProjectMetadataDto metadata, List<PageDto> pages)
{
}
