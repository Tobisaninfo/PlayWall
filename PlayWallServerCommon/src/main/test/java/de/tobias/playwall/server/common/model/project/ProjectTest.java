package de.tobias.playwall.server.common.model.project;

import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class ProjectTest
{
	static Stream<Arguments> arguments()
	{
		return Stream.of(
				of(Project.builder()
						.metadata(ProjectMetadata.builder().id(UUID.randomUUID()).name("Demo").volume(1.0).build())
						.build()),
				of(Project.builder()
						.metadata(ProjectMetadata.builder().id(UUID.randomUUID()).name("Demo").volume(1.0).build())
						.pages(List.of(Page.builder().id(UUID.randomUUID()).name("Page 1").position(0).build()))
						.build()),
				of(Project.builder()
						.metadata(ProjectMetadata.builder().id(UUID.randomUUID()).name("Demo").volume(1.0).build())
						.pages(List.of(Page.builder().id(UUID.randomUUID()).name("Page 1").position(0).pads(
										List.of(Pad.builder().id(UUID.randomUUID()).name("Test 1")
												.content(AudioPadContent.builder().loop(true).volume(1.0).build())
												.build()))
								.build()))
						.build())
		);
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void testCopy(Project project)
	{
		final Project copy = project.copy();
		assertThat(project).isEqualTo(copy);
	}
}
