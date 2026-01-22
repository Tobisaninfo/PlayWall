package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService
{
	private final ProjectMetadataRepository projectMetadataRepository;
	private final ProjectRepository projectRepository;
	private final MessageSource messageSource;

	public List<ProjectMetadata> getAllProjectMetadata() throws IOException
	{
		projectMetadataRepository.loadAllProjectsMetadata();
		return projectMetadataRepository.getAllProjectMetadata();
	}

	public boolean deleteProjectById(UUID id) throws IOException
	{
		final boolean isSuccess = projectMetadataRepository.deleteProject(id);
		if(isSuccess)
		{
			return projectRepository.deleteProject(id);
		}

		return false;
	}

	public Project getProjectById(UUID id) throws IOException, ProjectNotExistsException
	{
		return projectRepository.loadProject(id);
	}

	/**
	 * Create and initially save the newly created project. The created project contains one page with empty pads.
	 *
	 * @param name                   Name of the project
	 * @param numberOfHorizontalPads Number of horizontal pads
	 * @param numberOfVerticalPads   Number of vertical pads
	 * @return created project
	 * @throws IOException                       persistence error
	 * @throws ProjectNameAlreadyExistsException project with the same name already exists
	 */
	public ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = projectMetadataRepository.addProject(name, numberOfHorizontalPads, numberOfVerticalPads);

		final Project project = Project.builder()
				.metadata(projectMetadata)
				.pages(new ArrayList<>())
				.build();
		addPage(project);

		projectRepository.saveProject(project);

		return projectMetadata;
	}

	public void rename(UUID projectId, String name) throws ProjectNameAlreadyExistsException, ProjectNotExistsException
	{
		projectMetadataRepository.renameProject(projectId, name);
	}

	public Page addPage(Project project)
	{
		final int nextPagePosition = project.getPages().size();

		String name;
		int pageNameIndex = nextPagePosition + 1;
		do
		{
			name = messageSource.getMessage("page.name.default", new Object[]{pageNameIndex}, LocaleContextHolder.getLocale());
			pageNameIndex++;
		}
		while(project.containsPageName(name));

		final Page page = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(new ArrayList<>())
				.build();

		for(int position = 0; position < project.getMetadata().getNumberOfPadsPerPage(); position++)
		{
			final Pad pad = Pad.builder().id(UUID.randomUUID()).position(position).build();
			page.getPads().add(pad);
		}

		project.getPages().add(page);
		return page;
	}

	public Page renamePage(Project project, UUID pageId, String newName) throws PageNotExistsException
	{
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}
		final Page page = pageOptional.get();
		page.setName(newName);

		return page;
	}

	public Page duplicatePage(Project project, UUID pageId, String name) throws PageNotExistsException
	{
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}
		final Page page = pageOptional.get();

		final List<Pad> newPads = page.getPads().stream()
				.map(Pad::copy)
				.toList();

		final int nextPagePosition = project.getPages().size();
		final Page newPage = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(newPads)
				.build();

		project.getPages().add(newPage);
		return newPage;
	}

	public boolean deletePage(Project project, UUID pageId)
	{
		return project.getPages().removeIf(page -> page.getId().equals(pageId));
	}
}
