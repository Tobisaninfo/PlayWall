package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectRenameRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(ProjectRenameRequest.class)
class ProjectRenameHandler implements OneTimeActionRequestHandler<ProjectRenameRequest>
{
	private final ProjectService projectService;
	private final ProjectController projectController;
	private final ApplicationContext context;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public void handleRequest(ProjectRenameRequest requestMessage) throws IOException
	{
		projectService.rename(requestMessage.getProjectId(), requestMessage.getNewName());

		if(requestMessage.getProjectId().equals(projectController.getLoadedProject().getMetadata().getId()))
		{
			projectController.getLoadedProject().getMetadata().setName(requestMessage.getNewName());
			context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(projectController.getLoadedProject().getMetadata())));
		}
	}
}