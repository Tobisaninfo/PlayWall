package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.migration.JsonMigrationResult;
import de.tobias.playwall.server.common.migration.MigrationRegistry;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.common.model.project.Views;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class ProjectRepository
{
	private static final String FILE_EXTENSION = ".json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;
	private final MigrationRegistry projectMigrationRegistry;

	public ProjectRepository(PathProvider pathProvider, JsonMapper mapper,
							 @Qualifier("projectMigrationRegistry") MigrationRegistry projectMigrationRegistry)
	{
		this.pathProvider = pathProvider;
		this.mapper = mapper;
		this.projectMigrationRegistry = projectMigrationRegistry;
	}

	public Project loadProject(UUID id) throws IOException, ProjectNotExistsException
	{
		log.info("Loading project {}", id);

		final Path path = getProjectPath(id);
		if(!Files.exists(path))
		{
			throw new ProjectNotExistsException(id);
		}

		final JsonNode root = mapper.readTree(Files.newBufferedReader(path));
		final JsonMigrationResult migrationResult = projectMigrationRegistry.migrate(root);
		final Project project = mapper.treeToValue(migrationResult.node(), Project.class);

		if(migrationResult.isMigrated())
		{
			saveProject(project);
		}

		return project;
	}

	public ProjectMetadata loadProjectMetadata(UUID id) throws IOException, ProjectNotExistsException
	{
		final Path path = getProjectPath(id);
		if(!Files.exists(path))
		{
			throw new ProjectNotExistsException(id);
		}

		final Project project = mapper.readerWithView(Views.IdAndNameOnly.class)
				.forType(Project.class)
				.readValue(Files.newBufferedReader(path));
		return project.getMetadata();
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
		return pathProvider.getPathForProject(id + FILE_EXTENSION);
	}
}