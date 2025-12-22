package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService
{
	private final ProjectMetadataRepository projectMetadataRepository;
	private final ProjectRepository projectRepository;

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

	public ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws IOException, ProjectNameAlreadyExistsException
	{
		final ProjectMetadata projectMetadata = projectMetadataRepository.addProject(name, numberOfHorizontalPads, numberOfVerticalPads);

		final Project project = Project.builder()
				.metadata(projectMetadata)
				.pages(new ArrayList<>())
				.build();

		project.getPages().add(Page.builder().id(UUID.randomUUID()).name("Page 1").position(0).pads(new ArrayList<>()).build()); // TODO: Move
		fillProjectPagesWithEmptyPads(project);

		projectRepository.saveProject(project);

		return projectMetadata;
	}

	public Page addPage(UUID id, String name) throws IOException, ProjectNotExistsException
	{
		return projectRepository.addPage(id, name);
	}

	public Page renamePage(UUID id, UUID pageId, String newName) throws IOException, PageNotExistsException, ProjectNotExistsException
	{
		return projectRepository.renamePage(id, pageId, newName);
	}

	public Page duplicatePage(UUID id, UUID pageId, String name) throws IOException, PageNotExistsException, ProjectNotExistsException
	{
		return projectRepository.duplicatePage(id, pageId, name);
	}

	public boolean deletePage(UUID id, UUID pageId) throws IOException, ProjectNotExistsException
	{
		return projectRepository.deletePage(id, pageId);
	}

	public void fillProjectPagesWithEmptyPads(Project project)
	{
		for(Page page : project.getPages())
		{
			for(int position = 0; position < project.getMetadata().getNumberOfPadsPerPage(); position++)
			{
				if(page.getPad(position) == null)
				{
					final Pad pad = Pad.builder().id(UUID.randomUUID()).position(position).build();
					page.getPads().add(pad);
				}
			}
		}
	}
}
