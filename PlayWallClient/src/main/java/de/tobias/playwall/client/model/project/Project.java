package de.tobias.playwall.client.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Project
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

	public Pad getPad(UUID padId)
	{
		return getPages().stream()
				.flatMap(page -> page.getPads().stream())
				.filter(pad -> pad.getId().equals(padId))
				.findFirst().orElse(null);
	}
}
