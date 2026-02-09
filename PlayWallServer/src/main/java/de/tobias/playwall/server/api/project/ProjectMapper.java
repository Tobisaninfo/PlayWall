package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.project.model.ProjectDto;
import de.tobias.playwall.server.api.page.PageMapper;
import de.tobias.playwall.server.common.model.project.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {PageMapper.class})
public interface ProjectMapper
{
	ProjectDto projectToProjectDto(Project project);

	Project projectDtoToProject(ProjectDto project);
}
