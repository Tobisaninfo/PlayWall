package de.tobias.playwall.client.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Project
{
	@Setter
	private ProjectMetadata metadata;
	private final List<Page> pages;

	public Page getPage(int position)
	{
		return pages.stream()
				.filter(page -> page.getPosition() == position)
				.findFirst()
				.orElse(null);
	}

	public Pad getPad(PadIndex index)
	{
		final Page page = getPage(index.getPagePosition());
		if(page != null)
		{
			return page.getPad(index.padPosition());
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
