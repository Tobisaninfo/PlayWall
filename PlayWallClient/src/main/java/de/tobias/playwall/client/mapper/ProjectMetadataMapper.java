package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.common.api.project.ProjectMetadataDto;

public class ProjectMetadataMapper
{
	public ProjectMetadataDao projectMetadataDtoToProjectMetadataDao(ProjectMetadataDto project)
	{
		return new ProjectMetadataDao(project.id(), project.name());
	}
}
