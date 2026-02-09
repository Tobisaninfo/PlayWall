package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectNameAlreadyExistsError;
import de.tobias.playwall.common.api.project.ProjectNotExistsError;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
import de.tobias.playwall.server.api.project.ProjectNotExistsException;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestHandlerTyped(ProjectSettingsUpdateRequest.class)
@RequiredArgsConstructor
class ProjectSettingsUpdateHandler implements UndoableRequestHandler<ProjectSettingsUpdateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;
	private final MessageSource messageSource;
	private final ApplicationContext context;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public Optional<UndoItem> handleRequest(ProjectSettingsUpdateRequest requestMessage) throws PlayWallServerException
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

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
			project.getMetadata().setTimeMode(requestMessage.getProjectMetadata().timeMode());
			project.getMetadata().setDefaultColor(requestMessage.getProjectMetadata().defaultColor());
			project.getMetadata().setPlayColor(requestMessage.getProjectMetadata().playColor());
			project.getMetadata().setNumberOfHorizontalPads(requestMessage.getProjectMetadata().numberOfHorizontalPads());
			project.getMetadata().setNumberOfVerticalPads(requestMessage.getProjectMetadata().numberOfVerticalPads());
			projectService.rename(project.getMetadata().getId(), requestMessage.getProjectMetadata().name());

			final List<UUID> removedPads = projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);
			removedPads.forEach(projectController::unloadAndRemovePad);

			context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(project.getMetadata())));

			return Optional.of(inverseOperation);
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

	private UndoItem getInverseOperation(ProjectSettingsUpdateRequest request)
	{
		final ProjectMetadataDto oldMetadata = projectMetadataMapper.projectMetadataToProjectMetadataDto(projectController.getLoadedProject().getMetadata());
		final String shortDescription = messageSource.getMessage("undo.description.short.project.settings", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.project.settings", new Object[]{}, LocaleContextHolder.getLocale());
		return new UndoItem(shortDescription, longDescription, request, new ProjectSettingsUpdateRequest(oldMetadata));
	}
}