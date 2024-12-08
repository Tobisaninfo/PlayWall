package de.tobias.playwall.server.api.project.model;

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
	private ProjectMetadata metadata;
	private List<Page> pages;

	public Optional<Page> getPageById(UUID pageId)
	{
		return getPages().stream()
				.filter(page -> page.getId().equals(pageId))
				.findFirst();
	}
}
