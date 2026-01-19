package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.client.domain.page.PageMapper;
import de.tobias.playwall.common.api.page.PageDto;
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
