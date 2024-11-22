package de.tobias.playwall.common.api.project;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PadMetadataDto(UUID id, String name, Integer position, PadStatus status)
{
}
