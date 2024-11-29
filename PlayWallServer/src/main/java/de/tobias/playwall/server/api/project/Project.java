package de.tobias.playwall.server.api.project;

import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class Project
{
	private UUID id;
	private String name;
	private int numberOfHorizontalPads;
	private int numberOfVerticalPads;
	private List<Page> pages;

	public Optional<Page> getPageById(UUID pageId)
	{
		return getPages().stream()
				.filter(page -> page.getId().equals(pageId))
				.findFirst();
	}
}
