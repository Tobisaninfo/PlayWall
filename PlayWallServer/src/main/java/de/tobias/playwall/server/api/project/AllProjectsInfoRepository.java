package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.model.project.AllProjectsInfo;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.model.project.Views;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

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
public class AllProjectsInfoRepository
{
	private static final String PROJECTS_FILENAME = "projects.json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;

	@Getter
	private AllProjectsInfo allProjectsInfo;

	public void loadAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		if(!Files.exists(path))
		{
			log.debug("No projects.json found, creating empty file in: \"{}\"", path);
			allProjectsInfo = AllProjectsInfo.builder().build();
			saveAllProjectsInfo();
		}

		allProjectsInfo = mapper.readerWithView(Views.AllProjectsInfo.class)
				.forType(AllProjectsInfo.class)
				.readValue(Files.newBufferedReader(path));
	}

	public void saveAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writerWithView(Views.AllProjectsInfo.class).writeValue(Files.newBufferedWriter(path), allProjectsInfo);
	}

	void clearProjects() throws IOException
	{
		this.allProjectsInfo.setAllProjectsMetadata(new ArrayList<>());
		this.allProjectsInfo.getRecentProjects().clear();
		saveAllProjectsInfo();
	}

	public List<ProjectMetadata> getAllProjectMetadata()
	{
		return this.allProjectsInfo.getAllProjectsMetadata();
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final boolean isSuccess = allProjectsInfo.getAllProjectsMetadata().removeIf(project -> project.getId().equals(id));
		if(isSuccess)
		{
			allProjectsInfo.getRecentProjects().removeIf(i -> i.equals(id));
			saveAllProjectsInfo();
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
				.volume(1.0)
				.build();
		allProjectsInfo.getAllProjectsMetadata().add(newProjectMetadata);
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
			while(isProjectNameUsed(project.getMetadata().getName()));
			project.getMetadata().setName(name);
		}

		allProjectsInfo.getAllProjectsMetadata().add(project.getMetadata());
		saveAllProjectsInfo();
	}

	public void renameProject(UUID id, String name) throws ProjectNameAlreadyExistsException, ProjectNotExistsException
	{
		final Optional<ProjectMetadata> existingProjectOptional = getProjectMetadataByName(name);
		if(existingProjectOptional.isPresent() && !existingProjectOptional.get().getId().equals(id))
		{
			throw new ProjectNameAlreadyExistsException(name);
		}

		getProjectMetadataById(id).setName(name);
	}

	public void onProjectOpened(UUID id)
	{
		allProjectsInfo.getRecentProjects().push(id);
	}

	public List<UUID> getRecentProjectIds()
	{
		return allProjectsInfo.getRecentProjects().stream().toList();
	}

	ProjectMetadata getProjectMetadataById(UUID id) throws ProjectNotExistsException
	{
		return allProjectsInfo.getAllProjectsMetadata().stream().filter(project -> project.getId().equals(id)).findFirst().orElseThrow(() -> new ProjectNotExistsException(id));
	}

	private Optional<ProjectMetadata> getProjectMetadataByName(String name)
	{
		return allProjectsInfo.getAllProjectsMetadata().stream().filter(project -> project.getName().equals(name)).findFirst();
	}

	private boolean isProjectNameUsed(String name)
	{
		return allProjectsInfo.getAllProjectsMetadata().stream().anyMatch(project -> project.getName().equals(name));
	}
}
