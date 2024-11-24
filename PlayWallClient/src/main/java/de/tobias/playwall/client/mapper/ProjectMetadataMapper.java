package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;

public class ProjectMetadataMapper
{
	public ProjectMetadata projectMetadataDtoToProjectMetadata(ProjectMetadataDto project)
	{
		return new ProjectMetadata(project.id(), project.name());
	}
}
