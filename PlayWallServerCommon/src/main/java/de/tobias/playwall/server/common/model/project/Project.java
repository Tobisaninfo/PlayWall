package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonView;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.page.PageSettings;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class Project
{
	@JsonView(Views.IdAndNameOnly.class)
	private ProjectMetadata metadata;
	private List<Page> pages;

	// TODO: Should it realy be an optional?
	public Optional<Page> getPageById(UUID pageId)
	{
		return getPages().stream()
				.filter(page -> page.getId().equals(pageId))
				.findFirst();
	}

	public Page getPageByPad(UUID padId)
	{
		return getPages().stream()
				.filter(page ->
						page.getPads().stream()
								.anyMatch(pad -> pad.getId().equals(padId))
				)
				.findFirst()
				.orElse(null);
	}

	public Pad getPad(UUID padId)
	{
		return getPages().stream()
				.flatMap(page -> page.getPads().stream())
				.filter(pad -> pad.getId().equals(padId))
				.findFirst().orElse(null);
	}

	public Map<UUID, Integer> getPagePositions()
	{
		return getPages().stream().collect(Collectors.toMap(
				Page::getId,
				Page::getPosition
		));
	}

	public boolean containsPageName(String name)
	{
		return pages.stream().map(Page::getSettings).map(PageSettings::getName).anyMatch(name::equals);
	}

	@SuppressWarnings("java:S6204")
	public Project copy(boolean generateNewId)
	{
		return Project.builder()
				.metadata(metadata.copy(generateNewId))
				.pages(pages == null ? null : pages.stream().map(page -> page.copy(generateNewId)).collect(Collectors.toList()))
				.build();
	}
}
