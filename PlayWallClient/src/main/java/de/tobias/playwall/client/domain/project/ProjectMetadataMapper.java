package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class ProjectMetadataMapper
{
	public ProjectMetadata projectMetadataDtoToProjectMetadata(ProjectMetadataDto project)
	{
		return new ProjectMetadata(project.id(), project.name(), project.numberOfHorizontalPads(), project.numberOfVerticalPads(), project.volume(), project.timeMode());
	}

	public ProjectMetadataDto projectMetadataToProjectMetadataDto(ProjectMetadata project)
	{
		return new ProjectMetadataDto(project.getId(), project.getName(), project.getNumberOfHorizontalPads(), project.getNumberOfVerticalPads(), project.getVolume(), project.getTimeMode());
	}
}
