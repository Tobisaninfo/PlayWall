package de.tobias.playwall.common.api.project.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMetadataDto(UUID id, String name, int numberOfHorizontalPads, int numberOfVerticalPads)
{
}
