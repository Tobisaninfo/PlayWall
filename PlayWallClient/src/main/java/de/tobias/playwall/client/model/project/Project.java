package de.tobias.playwall.client.model.project;

import java.util.List;
import java.util.UUID;

public record Project(UUID id, String name, int numberOfHorizontalPads, int numberOfVerticalPads, List<Page> pages)
{
}
