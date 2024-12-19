package de.tobias.playwall.client.model.project;

import java.util.List;

public record Project(ProjectMetadata metadata, List<Page> pages)
{
}
