package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.PadMetadataDto;
import de.tobias.playwall.common.api.project.PageMetadataDto;
import de.tobias.playwall.common.api.project.ProjectMetadataDto;

import java.util.ArrayList;
import java.util.List;

public class ProjectMetadataMapper
{
	public Project projectMetadataDtoToProject(ProjectMetadataDto project)
	{
		final List<Page> pages = new ArrayList<>();
		for(PageMetadataDto page : project.pages())
		{
			final List<Pad> pads = new ArrayList<>();
			for(PadMetadataDto pad : page.pads())
			{
				pads.add(new Pad(pad.id(), pad.name(), pad.position(), pad.status()));
			}

			pages.add(new Page(page.id(), page.name(), page.position(), pads));
		}

		return new Project(project.id(), project.name(), pages);
	}
}
