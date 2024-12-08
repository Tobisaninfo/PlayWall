package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.api.project.model.Pad;
import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.api.project.model.ProjectMetadata;
import de.tobias.playwall.server.storage.PathProvider;
import jakarta.annotation.PostConstruct;
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

	private List<ProjectMetadata> allProjectsMetadata;

	@PostConstruct
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

	public Optional<ProjectMetadata> addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException
	{
		final Optional<ProjectMetadata> existingProjectOptional = getProjectByName(name);
		if(existingProjectOptional.isPresent())
		{
			return Optional.empty();
		}

		final ProjectMetadata newProject = ProjectMetadata.builder()
				.id(UUID.randomUUID())
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.pages(new ArrayList<>())
				.build();
		allProjectsMetadata.add(newProject);
		saveProjects();

		return Optional.of(newProject);
	}

	public Page addPage(UUID projectId, String name) throws IOException, ProjectNotExistsException
	{
		final ProjectMetadata project = getProjectById(projectId);
		final int nextPagePosition = project.getPages().size();

		final Page page = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(new ArrayList<>())
				.build();

		project.getPages().add(page);
		saveProjects();

		return page;
	}

	public Page renamePage(UUID projectId, UUID pageId, String newName) throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final ProjectMetadata project = getProjectById(projectId);
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(projectId, pageId);
		}
		final Page page = pageOptional.get();
		page.setName(newName);

		saveProjects();

		return page;
	}

	public Page duplicatePage(UUID projectId, UUID pageId, String name) throws IOException, ProjectNotExistsException, PageNotExistsException
	{
		final ProjectMetadata project = getProjectById(projectId);
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(projectId, pageId);
		}
		final Page page = pageOptional.get();

		final List<Pad> newPads = page.getPads().stream()
				.map(pad -> Pad.builder()
						.id(UUID.randomUUID())
						.name(pad.getName())
						.position(pad.getPosition())
						.mediaPaths(pad.getMediaPaths())
						.build())
				.toList();

		final int nextPagePosition = project.getPages().size();
		final Page newPage = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(newPads)
				.build();

		project.getPages().add(newPage);
		saveProjects();

		return newPage;
	}

	public boolean deletePage(UUID projectId, UUID pageId) throws IOException, ProjectNotExistsException
	{
		final ProjectMetadata project = getProjectById(projectId);
		final boolean isSuccess = project.getPages().removeIf(page -> page.getId().equals(pageId));
		if(isSuccess)
		{
			saveProjects();
		}
		return isSuccess;
	}

	public ProjectMetadata getProjectById(UUID id) throws ProjectNotExistsException
	{
		return allProjectsMetadata.stream().filter(project -> project.getId().equals(id)).findFirst().orElseThrow(() -> new ProjectNotExistsException(id));
	}

	private Optional<ProjectMetadata> getProjectByName(String name)
	{
		return allProjectsMetadata.stream().filter(project -> project.getName().equals(name)).findFirst();
	}
}
