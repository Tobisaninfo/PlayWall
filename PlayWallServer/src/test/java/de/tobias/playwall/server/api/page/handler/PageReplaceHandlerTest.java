package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.pad.AudioPadContentDto;
import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.PadDto;
import de.tobias.playwall.common.api.page.PageDto;
import de.tobias.playwall.common.api.page.PageSettingsDto;
import de.tobias.playwall.common.api.page.request.PageReplaceRequest;
import de.tobias.playwall.common.api.page.update.PageReplaceUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.project.ProjectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class PageReplaceHandlerTest extends AbstractRequestHandlerTest
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
	private PageReplaceHandler handler;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@BeforeEach
	void init()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);

		projectController.unloadProject();
	}

	@Test
	void testReplacePage() throws Exception
	{
		final UUID padId = UUID.fromString("fc427184-2e55-4734-8148-5fb657963616");
		final String mediaPath = Paths.get(requireNonNull(getClass().getClassLoader().getResource("audio/example_1.mp3")).toURI()).toAbsolutePath().toString().replace("\\", "/");
		final Project project = TestUtils.loadProject(objectMapper, "projects/project_4.json");
		project.getPad(padId).setContent(AudioPadContent.builder().mediaPath(mediaPath).loop(false).build());
		projectController.loadProject(project).get();

		final Page oldPage = project.getPages().getFirst();

		// Verify old controllers existing
		assertThat(oldPage.getPads().stream().filter(pad -> pad.getContent() != null))
				.isNotEmpty()
				.allSatisfy(pad -> assertThat(projectController.getPadController(pad.getId())).isNotNull());


		final PageDto pageDto = PageDto.builder()
				.id(UUID.fromString("1e76b8b3-2d58-4533-aa57-e2b66360e9ea"))
				.settings(PageSettingsDto.builder()
						.name("New Page")
						.build())
				.position(3)
				.pads(IntStream.range(0, project.getMetadata().getNumberOfPadsPerPage())
						.mapToObj(i -> PadDto.builder().id(UUID.randomUUID()).position(i).build())
						.collect(Collectors.toList())
				)
				.build();
		// Set content to first new pad
		final PadDto padDto = pageDto.pads().getFirst();
		padDto.setContent(AudioPadContentDto.builder().mediaPath(mediaPath).loop(false).build());

		handler.handleRequest(new PageReplaceRequest(0, pageDto));

		//Verify old controllers are removed
		assertThat(project.getPageById(oldPage.getId())).isEmpty();
		assertThat(oldPage.getPads())
				.isNotEmpty()
				.allSatisfy(pad -> assertThat(projectController.getPadController(pad.getId())).isNull());

		// Verify the replaced page
		assertThat(project.getPages()).hasSize(2)
				.extracting(Page::getId)
				.containsExactly(
						pageDto.id(),
						UUID.fromString("209e515b-4237-4fc3-968b-79da4356836d")
				);
		final Page replacedPage = project.getPages().getFirst();
		assertThat(replacedPage.getId()).isEqualTo(pageDto.id());
		assertThat(replacedPage.getPosition()).isZero();

		// Verify new controllers are loaded
		assertThat(projectController.getPadController(padDto.getId())).isNotNull()
				.satisfies(controller -> assertThat(controller.getStatus()).isEqualTo(PadControllerStatus.READY));

		// Verify update message
		assertThat(applicationEvents.stream(PageReplaceUpdate.class))
				.hasSize(1)
				.last()
				.satisfies(event -> assertThat(event.getPage().id()).isEqualTo(pageDto.id()));
	}
}