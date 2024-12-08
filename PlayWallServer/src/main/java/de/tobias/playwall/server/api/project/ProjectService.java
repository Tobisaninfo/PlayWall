package de.tobias.playwall.server.api.project;

import de.tobias.playwall.server.api.project.model.Page;
import de.tobias.playwall.server.api.project.model.Project;
import de.tobias.playwall.server.api.project.model.ProjectMetadata;
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

	void clearProjects() throws IOException
	{
		final List<ProjectMetadata> allProjectMetadata = projectMetadataRepository.getAllProjectMetadata();

		projectMetadataRepository.clearProjects();
		for(ProjectMetadata projectMetadata : allProjectMetadata)
		{
			projectRepository.deleteProject(projectMetadata.getId());
		}
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
}
