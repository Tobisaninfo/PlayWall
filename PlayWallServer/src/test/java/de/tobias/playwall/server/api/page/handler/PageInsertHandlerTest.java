package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.pad.AudioPadContentDto;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.api.page.request.PageInsertRequest;
import de.tobias.playwall.common.api.page.update.PageInsertUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.DuplicatedIdException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageInsertHandlerTest extends AbstractRequestHandlerTest
{
	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectController projectController;

	@MockitoSpyBean
	private ProjectService projectService;

	@Autowired
	private JsonMapper objectMapper;

	@Autowired
	private PageInsertHandler handler;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		projectController.unloadProject();
	}

	static Stream<Arguments> arguments()
	{
		return Stream.of(
				of(0, Map.of(
								UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 0,
								UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 1,
								UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 2
						)
				),
				of(1, Map.of(
								UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 0,
								UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 1,
								UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 2
						)
				),
				of(2, Map.of(
								UUID.fromString("5eee891b-7e4e-451a-b4be-116770f73677"), 0,
								UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d"), 1,
								UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"), 2
						)
				)
		);
	}

	@ParameterizedTest
	@MethodSource("arguments")
	void testInsertPageFirst(int index, Map<UUID, Integer> positions) throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		projectController.loadProject(project).get();

		final PageDto pageDto = PageDto.builder()
				.id(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"))
				.name("New Page")
				.position(3)
				.pads(IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> PadDto.builder().id(UUID.randomUUID()).position(i).build())
						.collect(Collectors.toList())
				)
				.build();
		// Set content to first new pad
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString();
		final PadDto padDto = pageDto.pads().getFirst();
		padDto.setContent(AudioPadContentDto.builder().mediaPath(mediaPath).loop(false).build());

		handler.handleRequest(new PageInsertRequest(index, pageDto));

		assertThat(project.getPages()).hasSize(3)
				.element(index)
				.satisfies(page -> {
					assertThat(page.getId()).isEqualTo(pageDto.id());
					assertThat(page.getPosition()).isEqualTo(index);
				});

		assertThat(projectController.getPadController(padDto.getId())).isNotNull()
				.satisfies(controller -> assertThat(controller.getStatus()).isEqualTo(PadControllerStatus.READY));

		assertThat(applicationEvents.stream(PageInsertUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> {
					assertThat(event.getPage().id()).isEqualTo(pageDto.id());
					assertThat(event.getPositions()).hasSize(3)
							.containsExactlyInAnyOrderEntriesOf(positions);
				});
	}

	@Test
	void testInsertPageWithExistingId() throws Exception
	{
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_1.json");
		projectController.loadProject(project).get();

		final PageDto pageDto = PageDto.builder()
				.id(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"))
				.name("New Page")
				.position(3)
				.pads(IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> PadDto.builder().id(UUID.randomUUID()).position(i).build())
						.collect(Collectors.toList())
				)
				.build();

		final PageInsertRequest request = new PageInsertRequest(0, pageDto);
		assertThatThrownBy(() -> handler.handleRequest(request))
				.isInstanceOf(DuplicatedIdException.class);
	}
}