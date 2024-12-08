package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.server.api.project.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectMapper
{
	ProjectDto projectToProjectDto(Project project);
}
