package de.tobias.playwall.server.common.model.project;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest
{
	@Test
	void testCopy()
	{
		final Project project = Instancio.create(Project.class);
		final Project copy = project.copy();

		assertThat(copy)
				.usingRecursiveComparison()
				.ignoringFieldsMatchingRegexes(".*id")
				.isEqualTo(project);
	}
}
