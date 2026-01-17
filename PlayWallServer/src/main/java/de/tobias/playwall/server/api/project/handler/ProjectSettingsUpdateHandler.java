package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.history.UndoItem;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(ProjectSettingsUpdateRequest.class)
@RequiredArgsConstructor
public class ProjectSettingsUpdateHandler extends UndoableRequestHandler<ProjectSettingsUpdateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final MessageSource messageSource;
	private final ApplicationContext context;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public void handleUndoableRequest(ProjectSettingsUpdateRequest requestMessage) throws PlayWallServerException
	{
		final Project project = projectController.getLoadedProject();
		if(project == null)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}

		final ProjectMetadata oldMetadata = project.getMetadata().copy(false);

		try
		{
			project.getMetadata().setName(requestMessage.getProjectMetadata().name());
			projectService.rename(project.getMetadata().getId(), requestMessage.getProjectMetadata().name());

			context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(project.getMetadata())));
		}
		catch(ProjectNameAlreadyExistsException _)
		{
			project.setMetadata(oldMetadata);

			final ProjectNameAlreadyExistsError error = new ProjectNameAlreadyExistsError(requestMessage.getProjectMetadata().name());
			throw new PlayWallServerException(messageSource, error);
		}
		catch(ProjectNotExistsException _)
		{
			final ProjectNotExistsError error = new ProjectNotExistsError(project.getMetadata().getId());
			throw new PlayWallServerException(messageSource, error);
		}
	}

	@Override
	public UndoItem getInverseOperation(ProjectSettingsUpdateRequest request)
	{
		final ProjectMetadataDto oldMetadata = projectMetadataMapper.projectMetadataToProjectMetadataDto(projectController.getLoadedProject().getMetadata());
		return new UndoItem("Projekteinstellungen", request, new ProjectSettingsUpdateRequest(oldMetadata));
	}
}