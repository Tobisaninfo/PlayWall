package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.di.Service;
import de.tobias.playwall.client.di.InjectConstructor;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.api.project.model.ProjectDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class ProjectMapper
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final PageMapper pageMapper;

	public Project projectDtoToProject(ProjectDto projectDto)
	{
		final List<Page> pages = new ArrayList<>();
		for(PageDto pagedto : projectDto.pages())
		{
			pages.add(this.pageMapper.pageDtoToPage(pagedto));
		}

		return new Project(projectMetadataMapper.projectMetadataDtoToProjectMetadata(projectDto.metadata()), pages);
	}
}
