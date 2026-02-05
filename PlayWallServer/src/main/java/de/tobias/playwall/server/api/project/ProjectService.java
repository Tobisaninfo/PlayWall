package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.page.PageNameAlreadyExistsException;
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
import java.util.*;

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

	public void insertPage(Project project, Page page, int index)
	{
		if(project.getPageById(page.getId()).isPresent())
		{
			throw new DuplicatedIdException(page.getId());
		}

		project.getPages().add(index, page);
		for(int i = index; i < project.getPages().size(); i++)
		{
			project.getPages().get(i).setPosition(i);
		}
	}

	public void replacePage(Project project, Page page, int index)
	{
		page.setPosition(index);
		project.getPages().set(index, page);
	}

	public Map<UUID, Integer> getPageOrder(Project project)
	{
		final Map<UUID, Integer> pageOrder = new HashMap<>();
		for(Page page : project.getPages())
		{
			pageOrder.put(page.getId(), page.getPosition());
		}
		return pageOrder;
	}

	public void reorderPages(Project project, Map<UUID, Integer> pages)
	{
		project.getPages().forEach(page -> page.setPosition(pages.get(page.getId())));
		project.getPages().sort(Comparator.comparing(Page::getPosition));
	}

	public void renamePage(Project project, UUID pageId, String newName) throws PageNotExistsException, PageNameAlreadyExistsException
	{
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}
		final Page page = pageOptional.get();

		if(project.containsPageName(newName) && !page.getName().equals(newName))
		{
			throw new PageNameAlreadyExistsException(pageId, newName);
		}

		page.setName(newName);
	}

	public Page duplicatePage(Project project, UUID pageId) throws PageNotExistsException
	{
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}
		final Page page = pageOptional.get();

		String name;
		int copyIndex = 1;
		do
		{
			name = messageSource.getMessage("page.name.duplicate", new Object[]{page.getName(), copyIndex}, LocaleContextHolder.getLocale());
			copyIndex++;
		}
		while(project.containsPageName(name));

		final List<Pad> newPads = page.getPads().stream()
				.map(Pad::copy)
				.toList();

		final int nextPagePosition = page.getPosition() + 1;
		final Page newPage = Page.builder()
				.id(UUID.randomUUID())
				.name(name)
				.position(nextPagePosition)
				.pads(newPads)
				.build();

		project.getPages().add(nextPagePosition, newPage);
		for(int i = nextPagePosition + 1; i < project.getPages().size(); i++)
		{
			project.getPages().get(i).setPosition(i);
		}
		return newPage;
	}

	public boolean deletePage(Project project, UUID pageId)
	{
		final boolean removed = project.getPages().removeIf(page -> page.getId().equals(pageId));

		// Update remaining positions
		for(int i = 0; i < project.getPages().size(); i++)
		{
			project.getPages().get(i).setPosition(i);
		}

		return removed;
	}

	public void updateNumberOfPadsPerRowAndColumn(Project project, ProjectMetadata oldMetadata)
	{
		final int numberOfHorizontalPads = project.getMetadata().getNumberOfHorizontalPads();
		final int oldNumberOfHorizontalPads = oldMetadata.getNumberOfHorizontalPads();

		final int numberOfVerticalPads = project.getMetadata().getNumberOfVerticalPads();
		final int oldNumberOfVerticalPads = oldMetadata.getNumberOfVerticalPads();

		final int columnDifference = oldNumberOfHorizontalPads - numberOfHorizontalPads;
		final int rowDifference = oldNumberOfVerticalPads - numberOfVerticalPads;

		if(columnDifference == 0 && rowDifference == 0)
		{
			return;
		}

		for(Page page : project.getPages())
		{
			final List<Pad> pads = page.getPads();
			final List<Pad> resizedPads = new ArrayList<>();

			cleanPads(pads, oldNumberOfHorizontalPads, numberOfVerticalPads, numberOfHorizontalPads, resizedPads);
			addMissingPads(numberOfVerticalPads, numberOfHorizontalPads, resizedPads);

			page.setPads(resizedPads.stream().sorted(Comparator.comparing(Pad::getPosition)).toList());
		}
	}

	private static void cleanPads(List<Pad> pads, int oldNumberOfHorizontalPads, int numberOfVerticalPads, int numberOfHorizontalPads, List<Pad> resizedPads)
	{
		for(Pad pad : pads)
		{
			int oldPosition = pad.getPosition();
			int row = oldPosition / oldNumberOfHorizontalPads;
			int col = oldPosition % oldNumberOfHorizontalPads;

			if(row < numberOfVerticalPads && col < numberOfHorizontalPads)
			{
				int newPosition = row * numberOfHorizontalPads + col;
				pad.setPosition(newPosition);
				resizedPads.add(pad);
			}
		}
	}

	private static void addMissingPads(int numberOfVerticalPads, int numberOfHorizontalPads, List<Pad> resizedPads)
	{
		for(int row = 0; row < numberOfVerticalPads; row++)
		{
			for(int col = 0; col < numberOfHorizontalPads; col++)
			{
				int position = row * numberOfHorizontalPads + col;

				boolean exists = resizedPads.stream().anyMatch(p -> p.getPosition() == position);
				if(!exists)
				{
					resizedPads.add(Pad.builder()
							.id(UUID.randomUUID())
							.position(position)
							.build());
				}
			}
		}
	}
}
