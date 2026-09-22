package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.migration.JsonMigrationEngine;
import de.tobias.playwall.server.common.migration.MigrationRegistry;
import de.tobias.playwall.server.common.model.project.AllProjectsInfo;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
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
public class AllProjectsInfoRepository
{
	private static final String PROJECTS_FILENAME = "projects.json";
	private static final String VERSION_FIELD_NAME = "VERSION";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;
	private final ProjectRepository projectRepository;
	private final JsonMigrationEngine allProjectsInfoMigrationEngine;
	private final MigrationRegistry allProjectsInfoMigrationRegistry;

	private AllProjectsInfo allProjectsInfo;

	public AllProjectsInfoRepository(PathProvider pathProvider, JsonMapper mapper, ProjectRepository projectRepository,
			@Qualifier("allProjectsInfoMigrationEngine") JsonMigrationEngine allProjectsInfoMigrationEngine,
			@Qualifier("allProjectsInfoMigrationRegistry") MigrationRegistry allProjectsInfoMigrationRegistry)
	{
		this.pathProvider = pathProvider;
		this.mapper = mapper;
		this.projectRepository = projectRepository;
		this.allProjectsInfoMigrationEngine = allProjectsInfoMigrationEngine;
		this.allProjectsInfoMigrationRegistry = allProjectsInfoMigrationRegistry;
	}

	public AllProjectsInfo getAllProjectsInfo()
	{
		if(allProjectsInfo == null)
		{
			try
			{
				loadAllProjectsInfo();
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		return allProjectsInfo;
	}

	public void loadAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		if(!Files.exists(path))
		{
			log.debug("No projects.json found, creating empty file in: \"{}\"", path);
			allProjectsInfo = AllProjectsInfo.builder().build();
			saveAllProjectsInfo();
			return;
		}

		final JsonNode root = mapper.readTree(Files.newBufferedReader(path));
		addVersionIfMissing((ObjectNode) root);

		final JsonNode migrated = allProjectsInfoMigrationEngine.migrate(root);
		allProjectsInfo = mapper.treeToValue(migrated, AllProjectsInfo.class);

		if(root.path(VERSION_FIELD_NAME).asInt() < allProjectsInfoMigrationRegistry.currentVersion())
		{
			saveAllProjectsInfo();
		}
	}

	public void saveAllProjectsInfo() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(PROJECTS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), getAllProjectsInfo());
	}

	/**
	 * Temporary helper: Adds the VERSION marker for legacy projects.json files that predate format versioning.
	 * Can be removed once all legacy files have been migrated to a versioned format.
	 */
	@Deprecated(since="8.3.0", forRemoval = true)
	private void addVersionIfMissing(ObjectNode root)
	{
		if(!root.has(VERSION_FIELD_NAME))
		{
			root.put(VERSION_FIELD_NAME, allProjectsInfoMigrationRegistry.getMinSupportedVersion());
		}
	}

	void clearProjects() throws IOException
	{
		final AllProjectsInfo copy = getAllProjectsInfo();
		copy.setAllProjects(new ArrayList<>());
		copy.getRecentProjects().clear();
		saveAllProjectsInfo();
	}

	public List<UUID> getAllProjects()
	{
		return getAllProjectsInfo().getAllProjects();
	}

	public boolean deleteProject(UUID id) throws IOException
	{
		final boolean isSuccess = getAllProjectsInfo().getAllProjects().remove(id);
		if(isSuccess)
		{
			getAllProjectsInfo().getRecentProjects().removeIf(i -> i.equals(id));
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
		getAllProjectsInfo().getAllProjects().add(newProjectMetadata.getId());
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

		getAllProjectsInfo().getAllProjects().add(project.getMetadata().getId());
		saveAllProjectsInfo();
	}

	public void onProjectOpened(UUID id)
	{
		getAllProjectsInfo().getRecentProjects().push(id);
	}

	public List<UUID> getRecentProjectIds()
	{
		return getAllProjectsInfo().getRecentProjects().stream().toList();
	}

	public List<ProjectMetadata> getAllProjectMetadata()
	{
		return getAllProjectsInfo().getAllProjects().stream().map(id -> {
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
