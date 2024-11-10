package de.tobias.playwall.common.net.project;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProjectMetadataDto(UUID id, String name)
{
}
