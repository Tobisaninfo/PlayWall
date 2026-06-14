package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.model.project.AllProjectsInfo;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllProjectsInfoRepository
{
	private static final String PROJECTS_FILENAME = "projects.json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;
	private final ProjectRepository projectRepository;

	@Getter
	private AllProjectsInfo allProjectsInfo;

	@PostConstruct
	void init() throws IOException
	{
		loadAllProjectsInfo();
	}

	public void loadAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		if(!Files.exists(path))
		{
			log.debug("No projects.json found, creating empty file in: \"{}\"", path);
			allProjectsInfo = AllProjectsInfo.builder().build();
			saveAllProjectsInfo();
		}

		allProjectsInfo = mapper.readValue(Files.newBufferedReader(path), AllProjectsInfo.class);
	}

	public void saveAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), allProjectsInfo);
	}

	void clearProjects() throws IOException
	{
		this.allProjectsInfo.setAllProjects(new ArrayList<>());
		this.allProjectsInfo.getRecentProjects().clear();
		saveAllProjectsInfo();
	}

	public List<UUID> getAllProjects()
	{
		return this.allProjectsInfo.getAllProjects();
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final boolean isSuccess = allProjectsInfo.getAllProjects().remove(id);
		if(isSuccess)
		{
			allProjectsInfo.getRecentProjects().removeIf(i -> i.equals(id));
			saveAllProjectsInfo();
		}
		return isSuccess;
	}

	public ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException, ProjectNameAlreadyExistsException
	{
		boolean projectNameUsed = isProjectNameUsed(name);
		if(projectNameUsed)
		{
			throw new ProjectNameAlreadyExistsException(name);
		}

		final ProjectMetadata newProjectMetadata = ProjectMetadata.builder()
				.id(UUID.randomUUID())
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.build();
		allProjectsInfo.getAllProjects().add(newProjectMetadata.getId());
		saveAllProjectsInfo();

		return newProjectMetadata;
	}

	public void importProject(Project project) throws IOException
	{
		project.getMetadata().setId(UUID.randomUUID());

		if(isProjectNameUsed(project.getMetadata().getName()))
		{
			String name;
			int copyIndex = 1;
			do
			{
				name = project.getMetadata().getName() + " " + copyIndex;
				copyIndex++;
			}
			while(isProjectNameUsed(name));
			project.getMetadata().setName(name);
		}

		allProjectsInfo.getAllProjects().add(project.getMetadata().getId());
		saveAllProjectsInfo();
	}

	public void onProjectOpened(UUID id)
	{
		allProjectsInfo.getRecentProjects().push(id);
	}

	public List<UUID> getRecentProjectIds()
	{
		return allProjectsInfo.getRecentProjects().stream().toList();
	}

	public List<ProjectMetadata> getAllProjectMetadata()
	{
		return allProjectsInfo.getAllProjects().stream().map(id -> {
			try
			{
				return projectRepository.loadProjectMetadata(id);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}).toList();
	}

	ProjectMetadata getProjectMetadataById(UUID id) throws ProjectNotExistsException
	{
		return getAllProjectMetadata().stream().filter(project -> project.getId().equals(id)).findFirst().orElseThrow(() -> new ProjectNotExistsException(id));
	}

	public Optional<ProjectMetadata> getProjectMetadataByName(String name)
	{
		return getAllProjectMetadata().stream().filter(project -> project.getName().equals(name)).findFirst();
	}

	public boolean isProjectNameUsed(String name)
	{
		return getAllProjectMetadata().stream().anyMatch(project -> project.getName().equals(name));
	}
}
