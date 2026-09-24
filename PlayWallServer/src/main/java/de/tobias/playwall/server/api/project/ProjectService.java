package de.tobias.playwall.server.api.project;

import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.server.api.page.PageNameAlreadyExistsException;
import de.tobias.playwall.server.api.page.PageNotExistsException;
import de.tobias.playwall.server.common.migration.MigrationException;
import de.tobias.playwall.server.common.migration.MigrationRegistry;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.page.PageSettings;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class ProjectService
{
	private final AllProjectsInfoRepository allProjectsInfoRepository;
	private final ProjectRepository projectRepository;
	private final MessageSource messageSource;
	private final JsonMapper jsonMapper;
	private final MigrationRegistry projectMigrationRegistry;

	public ProjectService(AllProjectsInfoRepository allProjectsInfoRepository,
						  ProjectRepository projectRepository,
						  MessageSource messageSource,
						  JsonMapper jsonMapper,
						  @Qualifier("projectMigrationRegistry") MigrationRegistry projectMigrationRegistry)
	{
		this.allProjectsInfoRepository = allProjectsInfoRepository;
		this.projectRepository = projectRepository;
		this.messageSource = messageSource;
		this.jsonMapper = jsonMapper;
		this.projectMigrationRegistry = projectMigrationRegistry;
	}

	public boolean deleteProjectById(UUID id) throws IOException
	{
		final boolean isSuccess = allProjectsInfoRepository.deleteProject(id);
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
		final ProjectMetadata projectMetadata = allProjectsInfoRepository.addProject(name, numberOfHorizontalPads, numberOfVerticalPads);

		final Project project = Project.builder()
				.metadata(projectMetadata)
				.pages(new ArrayList<>())
				.build();
		addPage(project);

		projectRepository.saveProject(project);

		return projectMetadata;
	}

	public void rename(UUID projectId, String name) throws ProjectNameAlreadyExistsException, ProjectNotExistsException, IOException
	{
		final Optional<ProjectMetadata> existingProjectOptional = allProjectsInfoRepository.getProjectMetadataByName(name);
		if(existingProjectOptional.isPresent() && !existingProjectOptional.get().getId().equals(projectId))
		{
			throw new ProjectNameAlreadyExistsException(name);
		}

		final Project project = projectRepository.loadProject(projectId);
		project.getMetadata().setName(name);
		projectRepository.saveProject(project);
	}

	public void onProjectOpened(UUID id) throws IOException
	{
		allProjectsInfoRepository.onProjectOpened(id);
		allProjectsInfoRepository.saveAllProjectsInfo();
	}

	public UUID duplicateProject(UUID projectId) throws IOException
	{
		final Project project = projectRepository.loadProject(projectId);

		allProjectsInfoRepository.importProject(project);
		projectRepository.saveProject(project);

		return project.getMetadata().getId();
	}

	public byte[] exportProject(UUID projectId) throws IOException
	{
		return projectRepository.getProjectFile(projectId);
	}

	public UUID importProject(String mimetype, byte[] data) throws IOException
	{
		final MimeType mimeType = MimeType.valueOf(mimetype);
		if(!mimeType.equals(MimeTypeUtils.APPLICATION_JSON))
		{
			throw new IllegalArgumentException("Unsupported mimetype: " + mimeType);
		}

		final JsonNode root = parseJson(data);
		final int version;
		try
		{
			version = projectMigrationRegistry.parseVersion(root);
		}
		catch(MigrationException e)
		{
			throw new IllegalArgumentException(messageSource.getMessage("project.import.error.parse_version", new Object[]{}, LocaleContextHolder.getLocale()), e);
		}

		if(version < projectMigrationRegistry.getMinSupportedVersion())
		{
			throw new IllegalArgumentException(messageSource.getMessage("project.import.error.version.too_old", new Object[]{version, projectMigrationRegistry.getMinSupportedVersion()}, LocaleContextHolder.getLocale()));
		}

		if(version > projectMigrationRegistry.currentVersion())
		{
			throw new IllegalArgumentException(messageSource.getMessage("project.import.error.parse_version", new Object[]{}, LocaleContextHolder.getLocale()));
		}

		final Project project = jsonMapper.treeToValue(projectMigrationRegistry.migrate(root), Project.class);

		allProjectsInfoRepository.importProject(project);
		projectRepository.saveProject(project);

		return project.getMetadata().getId();
	}

	private JsonNode parseJson(byte[] data)
	{
		try
		{
			return jsonMapper.readTree(data);
		}
		catch(Exception e)
		{
			log.debug("Error parsing project file", e);
			throw new IllegalArgumentException(messageSource.getMessage("project.import.error.parse_version", new Object[]{}, LocaleContextHolder.getLocale()), e);
		}
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
				.settings(PageSettings.builder()
						.name(name)
						.color(Color.GRAY1)
						.build())
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

		if(project.containsPageName(newName) && !page.getSettings().getName().equals(newName))
		{
			throw new PageNameAlreadyExistsException(pageId, newName);
		}

		page.getSettings().setName(newName);
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
			name = messageSource.getMessage("page.name.duplicate", new Object[]{page.getSettings().getName(), copyIndex}, LocaleContextHolder.getLocale());
			copyIndex++;
		}
		while(project.containsPageName(name));

		final List<Pad> newPads = page.getPads().stream()
				.map(pad -> pad.copy(true))
				.toList();

		final int nextPagePosition = page.getPosition() + 1;
		final Page newPage = Page.builder()
				.id(UUID.randomUUID())
				.settings(PageSettings.builder()
						.name(name)
						.color(Color.GRAY1)
						.build())
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

	public List<UUID> updateNumberOfPadsPerRowAndColumn(Project project, ProjectMetadata oldMetadata)
	{
		final int newNumberOfHorizontalPads = project.getMetadata().getNumberOfHorizontalPads();
		final int oldNumberOfHorizontalPads = oldMetadata.getNumberOfHorizontalPads();

		final int newNumberOfVerticalPads = project.getMetadata().getNumberOfVerticalPads();
		final int oldNumberOfVerticalPads = oldMetadata.getNumberOfVerticalPads();

		final int columnDifference = oldNumberOfHorizontalPads - newNumberOfHorizontalPads;
		final int rowDifference = oldNumberOfVerticalPads - newNumberOfVerticalPads;

		if(columnDifference == 0 && rowDifference == 0)
		{
			return List.of();
		}

		final List<UUID> removedPads = new ArrayList<>();
		for(Page page : project.getPages())
		{
			final List<Pad> pads = page.getPads();

			removedPads.addAll(cleanPads(pads, oldNumberOfHorizontalPads, newNumberOfVerticalPads, newNumberOfHorizontalPads));
			addMissingPads(pads, newNumberOfVerticalPads, newNumberOfHorizontalPads);

			pads.sort(Comparator.comparing(Pad::getPosition));
		}

		return removedPads;
	}

	private static List<UUID> cleanPads(List<Pad> pads, int oldNumberOfHorizontalPads, int newNumberOfVerticalPads, int newNumberOfHorizontalPads)
	{
		final List<UUID> removedPads = new ArrayList<>();

		final Iterator<Pad> iterator = pads.iterator();
		while(iterator.hasNext())
		{
			final Pad pad = iterator.next();

			int oldPosition = pad.getPosition();
			int row = oldPosition / oldNumberOfHorizontalPads;
			int col = oldPosition % oldNumberOfHorizontalPads;

			if(row < newNumberOfVerticalPads && col < newNumberOfHorizontalPads)
			{
				int newPosition = row * newNumberOfHorizontalPads + col;
				pad.setPosition(newPosition);
			}
			else
			{
				removedPads.add(pad.getId());
				iterator.remove();
			}
		}
		return removedPads;
	}

	private static void addMissingPads(List<Pad> pads, int newNumberOfVerticalPads, int newNumberOfHorizontalPads)
	{
		for(int row = 0; row < newNumberOfVerticalPads; row++)
		{
			for(int col = 0; col < newNumberOfHorizontalPads; col++)
			{
				int position = row * newNumberOfHorizontalPads + col;

				boolean exists = pads.stream().anyMatch(p -> p.getPosition() == position);
				if(!exists)
				{
					pads.add(Pad.builder()
							.id(UUID.randomUUID())
							.position(position)
							.build());
				}
			}
		}
	}
}
