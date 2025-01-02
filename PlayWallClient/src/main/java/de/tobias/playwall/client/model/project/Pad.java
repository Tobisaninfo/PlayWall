package de.tobias.playwall.client.model.project;

import de.tobias.playwall.common.api.project.model.PadStatus;

import java.util.List;
import java.util.UUID;

public record Pad(UUID id, String name, Integer position, PadStatus status, List<String> mediaPaths, Boolean isLoop, Double volume)
{
}
