package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractUndoableRequestHandlerTest;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RecordApplicationEvents
class ProjectUpdateHandlerTest extends AbstractUndoableRequestHandlerTest<ProjectUpdateRequest>
{
	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ApplicationEvents applicationEvents;

	@Autowired
	private ProjectMapper projectMapper;

	@Autowired
	private RequestExecutor requestExecutor;

	@BeforeEach
	void beforeEach()
	{
		final AudioHandler audioHandler = mock(AudioHandler.class);
		when(audioHandlerFactory.createAudioHandler(any())).thenReturn(audioHandler);
	}

	@Test
	void testHandleRequestSmaller() throws Exception
	{
		final Project previousProject = TestUtils.loadProject(objectMapper, "projects/project_6_1x1.json");
		final Project loadedProject = TestUtils.loadProject(objectMapper, "projects/project_6_2x2.json");
		projectController.loadProject(loadedProject).get();

		final ProjectUpdateRequest request = new ProjectUpdateRequest(projectMapper.projectToProjectDto(previousProject));
		reset(projectController);
		requestExecutor.execute(request);

		assertThat(loadedProject.getPages().getFirst().getPads())
				.hasSize(1)
				.satisfies(pad -> assertThat(pad.getFirst().getId()).isEqualTo(UUID.fromString("3a9edcfd-c4a0-48c5-a34b-d2b9cdc12225")))
				.satisfies(pad -> assertThat(pad.getFirst().getPosition()).isEqualTo(0));
		assertThat(loadedProject.getPages().get(1).getPads())
				.hasSize(1)
				.satisfies(pad -> assertThat(pad.getFirst().getId()).isEqualTo(UUID.fromString("0a589aed-3ead-4fe0-9c0a-4c7b1b83b8a6")))
				.satisfies(pad -> assertThat(pad.getFirst().getPosition()).isEqualTo(0));

		verify(projectController).unloadAndRemovePad(UUID.fromString("895082d5-3655-4aca-96db-818fef99e9ef"));
		verify(projectController).unloadAndRemovePad(UUID.fromString("f55f7691-2842-4d3f-9b64-08ddb0161398"));
		verify(projectController).unloadAndRemovePad(UUID.fromString("c37d6bb6-49a7-4b72-964d-dc9a8571507a"));
		verify(projectController).unloadAndRemovePad(UUID.fromString("14daf9a8-76ae-42c1-bef4-b9a273eac85d"));
		verify(projectController).unloadAndRemovePad(UUID.fromString("d7f7fec5-3a92-434b-90cb-4ceae54eed1c"));
		verify(projectController).unloadAndRemovePad(UUID.fromString("ed56c9c7-33dd-4d24-8cab-f115d26e6827"));

		assertThat(applicationEvents.stream(ProjectUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProject().pages().getFirst().pads()).hasSize(1))
				.satisfies(event -> assertThat(event.getProject().pages().get(1).pads()).hasSize(1));

		verify(projectController, never()).createNewPadController(any());
	}

	@Test
	void testHandleRequestLarger() throws Exception
	{
		final Project previousProject = TestUtils.loadProject(objectMapper, "projects/project_6_2x2.json");
		final Project loadedProject = TestUtils.loadProject(objectMapper, "projects/project_6_1x1.json");
		projectController.loadProject(loadedProject).get();

		final ProjectUpdateRequest request = new ProjectUpdateRequest(projectMapper.projectToProjectDto(previousProject));
		reset(projectController);
		requestExecutor.execute(request);

		assertThat(loadedProject.getPages().getFirst().getPads())
				.hasSize(4)
				.satisfies(pads -> assertThat(pads.getFirst().getId()).isEqualTo(UUID.fromString("3a9edcfd-c4a0-48c5-a34b-d2b9cdc12225")))
				.satisfies(pads -> assertThat(pads.getFirst().getPosition()).isEqualTo(0))
				.satisfies(pads -> assertThat(pads.get(1).getId()).isEqualTo(UUID.fromString("895082d5-3655-4aca-96db-818fef99e9ef")))
				.satisfies(pads -> assertThat(pads.get(1).getPosition()).isEqualTo(1))
				.satisfies(pads -> assertThat(pads.get(2).getId()).isEqualTo(UUID.fromString("f55f7691-2842-4d3f-9b64-08ddb0161398")))
				.satisfies(pads -> assertThat(pads.get(2).getPosition()).isEqualTo(2))
				.satisfies(pads -> assertThat(pads.get(3).getId()).isEqualTo(UUID.fromString("c37d6bb6-49a7-4b72-964d-dc9a8571507a")))
				.satisfies(pads -> assertThat(pads.get(3).getPosition()).isEqualTo(3));
		assertThat(loadedProject.getPages().get(1).getPads())
				.hasSize(4)
				.satisfies(pads -> assertThat(pads.getFirst().getId()).isEqualTo(UUID.fromString("0a589aed-3ead-4fe0-9c0a-4c7b1b83b8a6")))
				.satisfies(pads -> assertThat(pads.getFirst().getPosition()).isEqualTo(0))
				.satisfies(pads -> assertThat(pads.get(1).getId()).isEqualTo(UUID.fromString("14daf9a8-76ae-42c1-bef4-b9a273eac85d")))
				.satisfies(pads -> assertThat(pads.get(1).getPosition()).isEqualTo(1))
				.satisfies(pads -> assertThat(pads.get(2).getId()).isEqualTo(UUID.fromString("d7f7fec5-3a92-434b-90cb-4ceae54eed1c")))
				.satisfies(pads -> assertThat(pads.get(2).getPosition()).isEqualTo(2))
				.satisfies(pads -> assertThat(pads.get(3).getId()).isEqualTo(UUID.fromString("ed56c9c7-33dd-4d24-8cab-f115d26e6827")))
				.satisfies(pads -> assertThat(pads.get(3).getPosition()).isEqualTo(3));

		verify(projectController, never()).unloadAndRemovePad(any());

		assertThat(applicationEvents.stream(ProjectUpdate.class))
				.hasSize(1)
				.first()
				.satisfies(event -> assertThat(event.getProject().pages().getFirst().pads()).hasSize(4))
				.satisfies(event -> assertThat(event.getProject().pages().get(1).pads()).hasSize(4));

		verify(projectController, times(6)).createNewPadController(any());
	}
}
