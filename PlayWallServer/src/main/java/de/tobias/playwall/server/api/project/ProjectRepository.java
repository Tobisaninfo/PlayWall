package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.storage.PathProvider;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProjectRepository
{
	private final PathProvider pathProvider;
	private final ObjectMapper mapper;

	private List<ProjectMetadata> projects;

	@PostConstruct
	void loadProjects() throws IOException
	{
		final Path path = pathProvider.getPathForConfig("projects.json");
		projects = mapper.readValue(Files.newBufferedReader(path), new TypeReference<>()
		{
		});
	}

	void saveProjects() throws IOException
	{
		final Path path = pathProvider.getPathForConfig("projects.json");
		mapper.writeValue(Files.newBufferedWriter(path), projects);
	}

	public List<ProjectMetadata> getAllProjectMetadata()
	{
		return projects;
	}

	public boolean deleteProject(UUID id)
	{
		return projects.removeIf(project -> project.getId().equals(id));
	}
}
