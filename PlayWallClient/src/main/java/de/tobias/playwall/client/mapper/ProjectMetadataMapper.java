package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.di.Component;
import de.tobias.playwall.client.di.InjectConstructor;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class ProjectMetadataMapper
{
	public ProjectMetadata projectMetadataDtoToProjectMetadata(ProjectMetadataDto project)
	{
		return new ProjectMetadata(project.id(), project.name(), project.numberOfHorizontalPads(), project.numberOfVerticalPads());
	}
}
