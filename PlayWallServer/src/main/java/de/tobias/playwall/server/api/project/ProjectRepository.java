package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.storage.PathProvider;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ProjectRepository
{
	private final PathProvider pathProvider;
	private final ObjectMapper mapper;

	private List<Project> projects;

	@PostConstruct
	void loadProjects() throws IOException
	{
		final Path path = pathProvider.getPathForConfig("projects.json");
		if(!Files.exists(path))
		{
			log.debug("No projects.json found, creating empty file in: \"{}\"", path);
			saveProjects();
		}

		projects = mapper.readValue(Files.newBufferedReader(path), new TypeReference<>()
		{
		});
	}

	void saveProjects() throws IOException
	{
		final Path path = pathProvider.getPathForConfig("projects.json");
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), projects);
	}

	public List<Project> getAllProjectMetadata()
	{
		return projects;
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final boolean isSuccess = projects.removeIf(project -> project.getId().equals(id));
		if(isSuccess)
		{
			saveProjects();
		}
		return isSuccess;
	}

	public Optional<Project> addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException
	{
		final Optional<Project> existingProjectOptional = getProjectByName(name);
		if(existingProjectOptional.isPresent())
		{
			return Optional.empty();
		}

		final Project newProject = Project.builder()
				.id(UUID.randomUUID())
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.pages(new ArrayList<>())
				.build();
		projects.add(newProject);
		saveProjects();

		return Optional.of(newProject);
	}

	public Optional<Page> addPage(UUID projectId, String name) throws IOException
	{
		final Optional<Project> projectOptional = getProjectById(projectId);
		if(projectOptional.isEmpty())
		{
			return Optional.empty();
		}

		final Project project = projectOptional.get();
		final int nextPagePosition = project.getPages().size();

		final Page page = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(new ArrayList<>())
				.build();

		project.getPages().add(page);
		saveProjects();

		return Optional.of(page);
	}

	public Optional<Project> getProjectById(UUID id)
	{
		return projects.stream().filter(project -> project.getId().equals(id)).findFirst();
	}

	private Optional<Project> getProjectByName(String name)
	{
		return projects.stream().filter(project -> project.getName().equals(name)).findFirst();
	}
}
