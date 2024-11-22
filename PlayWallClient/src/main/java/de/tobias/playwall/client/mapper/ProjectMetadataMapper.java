package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.ProjectMetadataDto;

public class ProjectMetadataMapper
{
	public Project projectMetadataDtoToProjectMetadataDao(ProjectMetadataDto project)
	{
		return new Project(project.id(), project.name());
	}
}
