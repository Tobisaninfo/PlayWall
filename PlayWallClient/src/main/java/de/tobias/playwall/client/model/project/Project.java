package de.tobias.playwall.client.model.project;

import java.util.List;

public record Project(ProjectMetadata metadata, List<Page> pages)
{
	public Page getPage(int position)
	{
		return pages.stream().findFirst()
				.filter(page -> page.position() == position)
				.orElse(null);
	}

	public Pad getPad(PadIndex index)
	{
		final Page page = getPage(index.getPagePosition());
		if(page != null)
		{
			return page.getPad(index.id());
		}
		return null;
	}
}
