package de.tobias.playwall.client.model.project;

import java.util.List;
import java.util.UUID;

public record Page(UUID id, String name, Integer position, List<Pad> pads)
{
}
