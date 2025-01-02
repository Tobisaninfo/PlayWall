package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.storage.PathProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectMetadataRepository
{
	private static final String PROJECTS_FILENAME = "projects.json";

	private final PathProvider pathProvider;
	private final ObjectMapper mapper;

	private List<ProjectMetadata> allProjectsMetadata = new ArrayList<>();

	void loadAllProjectsMetadata() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		if(!Files.exists(path))
		{
			log.debug("No projects.json found, creating empty file in: \"{}\"", path);
			saveProjects();
		}

		allProjectsMetadata = mapper.readValue(Files.newBufferedReader(path), new TypeReference<>()
		{
		});
	}

	private void saveProjects() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), allProjectsMetadata);
	}

	void clearProjects() throws IOException
	{
		this.allProjectsMetadata = new ArrayList<>();
		saveProjects();
	}

	public List<ProjectMetadata> getAllProjectMetadata()
	{
		return allProjectsMetadata;
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final boolean isSuccess = allProjectsMetadata.removeIf(project -> project.getId().equals(id));
		if(isSuccess)
		{
			saveProjects();
		}
		return isSuccess;
	}

	public ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException, ProjectNameAlreadyExistsException
	{
		final Optional<ProjectMetadata> existingProjectOptional = getProjectMetadataByName(name);
		if(existingProjectOptional.isPresent())
		{
			throw new ProjectNameAlreadyExistsException(name);
		}

		final ProjectMetadata newProjectMetadata = ProjectMetadata.builder()
				.id(UUID.randomUUID())
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.build();
		allProjectsMetadata.add(newProjectMetadata);
		saveProjects();

		return newProjectMetadata;
	}

	ProjectMetadata getProjectMetadataById(UUID id) throws ProjectNotExistsException
	{
		return allProjectsMetadata.stream().filter(project -> project.getId().equals(id)).findFirst().orElseThrow(() -> new ProjectNotExistsException(id));
	}

	private Optional<ProjectMetadata> getProjectMetadataByName(String name)
	{
		return allProjectsMetadata.stream().filter(project -> project.getName().equals(name)).findFirst();
	}
}
