package de.tobias.playwall.client.domain.pad;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PadTest
{
	@Test
	void copy()
	{
		final Pad project = Instancio.create(Pad.class);
		final Pad copy = project.copy();

		assertThat(copy)
				.usingRecursiveComparison()
				.ignoringFieldsMatchingRegexes(".*id")
				.isEqualTo(project);
	}
}