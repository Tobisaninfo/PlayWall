package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectDuplicateRequest;
import de.tobias.playwall.common.api.project.request.ProjectDuplicateResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectDuplicateHandlerTest
{
	private final UUID projectId = UUID.randomUUID();

	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@Autowired
	private ProjectDuplicateHandler handler;

	@MockitoSpyBean
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@MockitoSpyBean
	private ProjectRepository projectRepository;

	@Autowired
	private JsonMapper jsonMapper;

	private Path projectFilePath;

	@BeforeEach
	void init() throws IOException
	{
		final Path projectListPath = tempDir.resolve("projects.json");
		projectFilePath = tempDir.resolve(projectId + ".json");
		when(pathProvider.getPathForProject(any())).thenReturn(projectFilePath);
		when(pathProvider.getPathForConfig(any())).thenReturn(projectListPath);

		doCallRealMethod().when(allProjectsInfoRepository).loadAllProjectsInfo();
		allProjectsInfoRepository.loadAllProjectsInfo();
	}

	@Test
	void testProjectDuplicate() throws IOException
	{
		doReturn(false).when(allProjectsInfoRepository).isProjectNameUsed(any());
		doReturn(true).when(allProjectsInfoRepository).isProjectNameUsed("Project 1");
		doCallRealMethod().when(allProjectsInfoRepository).importProject(any());
		doCallRealMethod().when(projectRepository).saveProject(any());
		doReturn(TestUtils.loadProject(jsonMapper, "projects/project_1.json")).when(projectRepository).loadProject(any());

		final Optional<ResponseMessage> response = handler.handleRequest(new ProjectDuplicateRequest(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c")));
		assertThat(response).isNotEmpty();

		assertThat(response.get()).isInstanceOf(ProjectDuplicateResponse.class)
				.extracting(ProjectDuplicateResponse.class::cast)
				// Not equal to UUID from project_1 from resources
				.satisfies(message -> assertThat(message.getProjectId()).isNotNull().isNotEqualTo(UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c")));

		assertThat(Files.exists(projectFilePath)).isTrue();

		final Project importedProject = TestUtils.loadProject(jsonMapper, projectFilePath);
		final Project expectedProject = TestUtils.loadProject(jsonMapper, "projects/project_1.json");

		assertThat(importedProject)
				.usingRecursiveComparison()
				.ignoringFieldsMatchingRegexes("metadata.id", "metadata.name")
				.isEqualTo(expectedProject);

		assertThat(importedProject.getMetadata().getName()).isEqualTo("Project 1 1");
	}
}
