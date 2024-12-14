package de.tobias.playwall.server.api.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.api.project.model.Pad;
import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.api.project.model.Project;
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
class ProjectRepository
{
	private static final String FILE_EXTENSION = ".json";

	private final PathProvider pathProvider;
	private final ObjectMapper mapper;

	Project loadProject(UUID id) throws IOException, ProjectNotExistsException
	{
		final Path path = pathProvider.getPathForConfig(id + FILE_EXTENSION);
		if(!Files.exists(path))
		{
			throw new ProjectNotExistsException(id);
		}

		return mapper.readValue(Files.newBufferedReader(path), new TypeReference<>()
		{
		});
	}

	void saveProject(Project project) throws IOException
	{
		final Path path = pathProvider.getPathForConfig(project.getMetadata().getId() + FILE_EXTENSION);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), project);
	}

	boolean deleteProject(UUID id) throws IOException
	{
		final Path path = pathProvider.getPathForConfig(id + FILE_EXTENSION);
		return Files.deleteIfExists(path);
	}

	Page addPage(UUID id, String name) throws IOException, ProjectNotExistsException
	{
		final Project project = loadProject(id);
		final int nextPagePosition = project.getPages().size();

		final Page page = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(new ArrayList<>())
				.build();

		project.getPages().add(page);
		saveProject(project);

		return page;
	}

	Page renamePage(UUID id, UUID pageId, String newName) throws IOException, PageNotExistsException, ProjectNotExistsException
	{
		final Project project = loadProject(id);
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}
		final Page page = pageOptional.get();
		page.setName(newName);

		saveProject(project);

		return page;
	}

	Page duplicatePage(UUID id, UUID pageId, String name) throws IOException, PageNotExistsException, ProjectNotExistsException
	{
		final Project project = loadProject(id);
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
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
		saveProject(project);

		return newPage;
	}

	boolean deletePage(UUID id, UUID pageId) throws IOException, ProjectNotExistsException
	{
		final Project project = loadProject(id);
		final boolean isSuccess = project.getPages().removeIf(page -> page.getId().equals(pageId));
		if(isSuccess)
		{
			saveProject(project);
		}
		return isSuccess;
	}
}
