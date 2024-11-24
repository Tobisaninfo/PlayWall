package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.model.PadDto;
import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.api.project.model.ProjectDto;

import java.util.ArrayList;
import java.util.List;

public class ProjectMapper
{
	public Project projectDtoToProject(ProjectDto project)
	{
		final List<Page> pages = new ArrayList<>();
		for(PageDto page : project.pages())
		{
			final List<Pad> pads = new ArrayList<>();
			for(PadDto pad : page.pads())
			{
				pads.add(new Pad(pad.id(), pad.name(), pad.position(), pad.status()));
			}

			pages.add(new Page(page.id(), page.name(), page.position(), pads));
		}

		return new Project(project.id(), project.name(), pages);
	}
}
