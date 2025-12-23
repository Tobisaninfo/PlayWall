package de.tobias.playwall.client.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public final class Project
{
	private final ProjectMetadata metadata;
	private final List<Page> pages;

	public Page getPage(int position)
	{
		return pages.stream().findFirst()
				.filter(page -> page.getPosition() == position)
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

	@Override
	public String toString()
	{
		return "Project[" +
			   "metadata=" + metadata + ", " +
			   "getPages=" + pages + ']';
	}

}
