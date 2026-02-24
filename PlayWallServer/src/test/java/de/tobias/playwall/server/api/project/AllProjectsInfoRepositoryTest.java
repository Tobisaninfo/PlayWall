package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AllProjectsInfoRepositoryTest
{
	@TempDir
	private Path tempDir;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@MockitoBean
	private PathProvider pathProvider;

	@BeforeEach
	void beforeEach() throws IOException
	{
		final Path projectsFile = tempDir.resolve("projects.json");
		when(pathProvider.getPathForConfig(any())).thenReturn(projectsFile);

		allProjectsInfoRepository.loadAllProjectsInfo();
		allProjectsInfoRepository.clearProjects();
	}

	@Autowired
	private AllProjectsInfoRepository allProjectsInfoRepository;

	@Test
	void test_noProjects()
	{
		assertThat(allProjectsInfoRepository.getAllProjectMetadata()).isEmpty();
	}

	@Test
	void test_addProject() throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = allProjectsInfoRepository.addProject("New ProjectMetadata", 6, 5);

		assertThat(projectMetadata)
				.extracting(ProjectMetadata::getName, ProjectMetadata::getNumberOfHorizontalPads, ProjectMetadata::getNumberOfVerticalPads, ProjectMetadata::getVolume)
				.containsExactly("New ProjectMetadata", 6, 5, 1.0);

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectMetadata.getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.build();

		assertThat(allProjectsInfoRepository.getAllProjectMetadata()).containsExactly(expected);
	}

	@Test
	void test_addProject_nameAlreadyExists() throws IOException, ProjectNameAlreadyExistsException
	{
		allProjectsInfoRepository.addProject("New ProjectMetadata", 6, 5);
		assertThatThrownBy(() -> allProjectsInfoRepository.addProject("New ProjectMetadata", 4, 3)).isInstanceOf(ProjectNameAlreadyExistsException.class);
	}

	@Test
	void test_getProjectMetadataById() throws IOException, ProjectNotExistsException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = allProjectsInfoRepository.addProject("New ProjectMetadata", 6, 5);

		final ProjectMetadata expected = ProjectMetadata.builder()
				.id(projectMetadata.getId())
				.name("New ProjectMetadata")
				.numberOfHorizontalPads(6)
				.numberOfVerticalPads(5)
				.volume(1.0)
				.build();

		assertThat(allProjectsInfoRepository.getProjectMetadataById(projectMetadata.getId())).isEqualTo(expected);
	}

	@Test
	void test_getProjectMetadataById_noMatch()
	{
		assertThatThrownBy(() -> allProjectsInfoRepository.getProjectMetadataById(UUID.randomUUID())).isInstanceOf(ProjectNotExistsException.class);
	}

	@Test
	void test_deleteProject() throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = allProjectsInfoRepository.addProject("New ProjectMetadata", 6, 5);
		assertThat(allProjectsInfoRepository.deleteProject(projectMetadata.getId())).isTrue();
		assertThat(allProjectsInfoRepository.getAllProjectMetadata()).isEmpty();
		assertThat(allProjectsInfoRepository.getRecentProjectIds()).isEmpty();
	}

	@Test
	void test_onProjectOpened()
	{
		allProjectsInfoRepository.onProjectOpened(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
		assertThat(allProjectsInfoRepository.getRecentProjectIds()).containsExactly(UUID.fromString("595775f1-20d6-4802-bd3d-2ed678effebe"));
	}

}
