package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.common.api.project.ProjectMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

public class ProjectMetadataMapper
{
	public ProjectMetadataDao projectMetadataDtoToProjectMapperDao(ProjectMetadataDto project)
	{
		return new ProjectMetadataDao(project.id(), project.name());
	}
}
