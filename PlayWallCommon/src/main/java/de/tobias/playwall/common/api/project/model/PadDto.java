package de.tobias.playwall.common.api.project.model;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PadDto(UUID id, String name, Integer position, PadStatus status, List<String> mediaPaths, Boolean isLoop, Double volume)
{
}
