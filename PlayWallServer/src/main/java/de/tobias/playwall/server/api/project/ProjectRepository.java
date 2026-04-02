package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRepository
{
	private static final String FILE_EXTENSION = ".json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;

	public Project loadProject(UUID id) throws IOException, ProjectNotExistsException
	{
		final Path path = getProjectPath(id);
		if(!Files.exists(path))
		{
			throw new ProjectNotExistsException(id);
		}

		return mapper.readValue(Files.newBufferedReader(path), Project.class);
	}

	public void saveProject(Project project) throws IOException
	{
		final Path path = getProjectPath(project.getMetadata().getId());
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), project);
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final Path path = getProjectPath(id);
		return Files.deleteIfExists(path);
	}

	public byte[] getProjectFile(UUID id) throws IOException
	{
		final Path path = getProjectPath(id);
		return Files.readAllBytes(path);
	}

	private Path getProjectPath(UUID id)
	{
		return pathProvider.getPathForConfig(id + FILE_EXTENSION);
	}
}
