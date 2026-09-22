package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.TestUtils;
import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.migration.MigrationException;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProjectRepositoryMigrationTest extends AbstractRequestHandlerTest
{
	private static final UUID PROJECT_ID = UUID.fromString("a09d1f3c-2384-4ee5-b13d-07f428efe35c");

	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private JsonMapper objectMapper;

	@BeforeEach
	void beforeEach()
	{
		when(pathProvider.getPathForProject(any())).thenReturn(tempDir.resolve(PROJECT_ID + ".json"));
	}

	@Test
	void testLoadProject_currentVersionIsLoadedAndNotRewritten() throws Exception
	{
		final String content = readResource("projects/project_1.json");
		final Path file = tempDir.resolve(PROJECT_ID + ".json");
		Files.writeString(file, content);

		final Project project = projectRepository.loadProject(PROJECT_ID);

		assertThat(project).isEqualTo(TestUtils.loadProject(objectMapper, "projects/project_1.json"));
		assertThat(Files.readString(file)).isEqualTo(content);
	}

	@Test
	void testLoadProject_missingVersionIsRejected() throws Exception
	{
		Files.writeString(tempDir.resolve(PROJECT_ID + ".json"), """
				{
					"metadata": {"id": "a09d1f3c-2384-4ee5-b13d-07f428efe35c", "name": "Project 1"},
					"pages": []
				}
				""");

		assertThatThrownBy(() -> projectRepository.loadProject(PROJECT_ID))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("Cannot determine version");
	}

	@Test
	void testLoadProject_tooOldVersionIsRejected() throws Exception
	{
		Files.writeString(tempDir.resolve(PROJECT_ID + ".json"), readResource("projects/project_too_old.json"));

		assertThatThrownBy(() -> projectRepository.loadProject(PROJECT_ID))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("too old to migrate");
	}

	@Test
	void testLoadProject_newerVersionIsRejected() throws Exception
	{
		Files.writeString(tempDir.resolve(PROJECT_ID + ".json"), readResource("projects/project_unsupported.json"));

		assertThatThrownBy(() -> projectRepository.loadProject(PROJECT_ID))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("newer than the supported version");
	}

	private static String readResource(String resource) throws IOException
	{
		try(InputStream in = ProjectRepositoryMigrationTest.class.getClassLoader().getResourceAsStream(resource))
		{
			return new String(Objects.requireNonNull(in, resource).readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}