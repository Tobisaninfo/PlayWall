package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMetadataMapper
{
	List<ProjectMetadataDto> projectMetadataToProjectMetadataDto(List<ProjectMetadata> projects);

	ProjectMetadataDto projectMetadataToProjectMetadataDto(ProjectMetadata project);
}
