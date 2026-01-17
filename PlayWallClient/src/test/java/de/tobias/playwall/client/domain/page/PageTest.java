package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.domain.pad.PadIndex;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.utils.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PageTest extends AbstractTest
{
	private Project project;

	@BeforeEach
	void init()
	{
		project = loadProject("projects/project_1.json");
	}

	private static Stream<Arguments> padIndexes()
	{
		return Stream.of(
				Arguments.of(new PadIndex(0, 0), UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")),
				Arguments.of(new PadIndex(1, 0), UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18")),
				Arguments.of(new PadIndex(30, 0), null),
				Arguments.of(new PadIndex(0, 1), UUID.fromString("33ee14cf-56d8-42bf-9aee-f5e3f22d2193")),
				Arguments.of(new PadIndex(1, 1), UUID.fromString("94ad54b2-995a-4da6-87c8-b86e5b97d03e")),
				Arguments.of(new PadIndex(0, 2), null)
		);
	}

	@ParameterizedTest
	@MethodSource("padIndexes")
	void testGetPadByIndex(PadIndex index, UUID expectedId)
	{
		if(expectedId == null)
		{
			assertThat(project.getPad(index)).isNull();
		}
		else
		{
			assertThat(project.getPad(index))
					.satisfies(pad -> assertThat(pad.getId()).isEqualTo(expectedId));
		}
	}

	@Test
	void testGetPadById()
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		assertThat(project.getPad(padId)).isNotNull()
				.satisfies(pad -> assertThat(pad.getId()).isEqualTo(padId));

		assertThat(project.getPad(UUID.fromString("fc427184-2e55-4734-8148-5fb657a63616"))).isNull();
	}
}
