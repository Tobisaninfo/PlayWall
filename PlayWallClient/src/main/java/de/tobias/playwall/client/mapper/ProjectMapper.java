package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.di.Component;
import de.tobias.playwall.client.di.DI;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.api.project.model.ProjectDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectMapper
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final PageMapper pageMapper;

	public ProjectMapper()
	{
		this.projectMetadataMapper = DI.instance().get(ProjectMetadataMapper.class);
		this.pageMapper = DI.instance().get(PageMapper.class);
	}

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
