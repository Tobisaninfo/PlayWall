package de.tobias.playwall.server.common.model.project;

import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest
{
	@Test
	void testCopyWithSameIds()
	{
		final Project project = Instancio.create(Project.class);
		final Project copy = project.copy(false);

		assertThat(copy)
				.usingRecursiveComparison()
				.ignoringFieldsMatchingRegexes(".*id")
				.isEqualTo(project);
	}

	@Test
	void testCopyWithNewIds()
	{
		final Project project = Instancio.create(Project.class);
		final Project copy = project.copy(true);

		assertThat(copy.getMetadata().getId()).isNotEqualTo(project.getMetadata().getId());
		assertThat(copy.getPages()).extracting(Page::getId).doesNotContain(project.getPages().stream().map(Page::getId).toArray(UUID[]::new));
		assertThat(copy.getPages().getFirst().getPads()).extracting(Pad::getId).doesNotContain(project.getPages().getFirst().getPads().stream().map(Pad::getId).toArray(UUID[]::new));
	}
}
