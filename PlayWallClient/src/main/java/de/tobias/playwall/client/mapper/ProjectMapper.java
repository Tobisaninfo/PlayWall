package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.api.project.model.ProjectDto;

import java.util.ArrayList;
import java.util.List;

public class ProjectMapper
{
	private final PageMapper pageMapper;

	public ProjectMapper(PageMapper pageMapper)
	{
		this.pageMapper = pageMapper;
	}

	public Project projectDtoToProject(ProjectDto project)
	{
		final List<Page> pages = new ArrayList<>();
		for(PageDto pagedto : project.pages())
		{
			pages.add(this.pageMapper.pageDtoToPage(pagedto));
		}

		return new Project(project.id(), project.name(), pages);
	}
}
