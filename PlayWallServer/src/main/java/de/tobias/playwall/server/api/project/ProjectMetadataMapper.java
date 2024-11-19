package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.ProjectMetadataDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMetadataMapper
{
	ProjectMetadataDto projectMetadataToProjectMetadataDto(ProjectMetadata project);
}
