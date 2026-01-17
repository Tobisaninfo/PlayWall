package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.domain.page.Page;
import de.tobias.playwall.utils.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest extends AbstractTest
{
	private Project project;

	@BeforeEach
	void init()
	{
		project = loadProject("projects/project_1.json");
	}

	@Test
	void testGetPage()
	{
		assertThat(project.getPage(0)).isNotNull()
				.satisfies(page -> assertThat(page.getId()).isEqualTo(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea")))
				.satisfies(page -> assertThat(page.getPosition()).isZero())
				.satisfies(page -> assertThat(page.getName()).isEqualTo("Page 1"));

		assertThat(project.getPage(1)).isNotNull()
				.satisfies(page -> assertThat(page.getId()).isEqualTo(UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e")))
				.satisfies(page -> assertThat(page.getPosition()).isEqualTo(1))
				.satisfies(page -> assertThat(page.getName()).isEqualTo("Page 2"));

		assertThat(project.getPage(3)).isNull();
	}

	@Test
	void testGetPages()
	{
		assertThat(project.getPages()).hasSize(2).extracting(Page::getId)
				.containsExactlyInAnyOrder(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), UUID.fromString("44c78975-7e53-432e-8526-bdcc5209c54e"));
	}

	private static Stream<Arguments> padIndexes()
	{
		return Stream.of(
				Arguments.of(0, UUID.fromString("fc427184-2e55-4734-8148-5fb657963616")),
				Arguments.of(1, UUID.fromString("57accabc-7d19-473c-a0a1-ea5c61b85e18")),
				Arguments.of(30, null)
		);
	}

	@ParameterizedTest
	@MethodSource("padIndexes")
	void testGetPadByIndex(Integer index, UUID expectedId)
	{
		final Page page = project.getPage(0);
		if(expectedId == null)
		{
			assertThat(page.getPad(index)).isNull();
		}
		else
		{
			assertThat(page.getPad(index))
					.satisfies(pad -> assertThat(pad.getId()).isEqualTo(expectedId));
		}
	}
}
