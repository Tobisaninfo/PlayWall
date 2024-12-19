package de.tobias.playwall.client.model.project;

import java.util.UUID;

public record ProjectMetadata(UUID id, String name, int numberOfHorizontalPads, int numberOfVerticalPads)
{
}
