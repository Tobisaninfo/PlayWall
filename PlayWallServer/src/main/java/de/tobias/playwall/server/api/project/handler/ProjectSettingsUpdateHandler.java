package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.request.ProjectUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.project.ProjectMapper;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectNameAlreadyExistsException;
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
import java.util.Objects;
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
	private final ProjectMapper projectMapper;

	@Override
	public Optional<UndoItem> handleRequest(ProjectSettingsUpdateRequest requestMessage)
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

		final Project project = projectController.getLoadedProject();

		final Project oldProject = project.copy(false);
		final ProjectMetadata oldMetadata = oldProject.getMetadata();

		try
		{
			final ProjectMetadata metadata = project.getMetadata();

			metadata.setName(requestMessage.getProjectMetadata().name());
			metadata.setTimeMode(requestMessage.getProjectMetadata().timeMode());
			metadata.setDefaultColor(requestMessage.getProjectMetadata().defaultColor());
			metadata.setPlayColor(requestMessage.getProjectMetadata().playColor());
			metadata.setEofWarningTime(requestMessage.getProjectMetadata().eofWarningTime());

			boolean hasProjectSizeChanged = !Objects.equals(oldMetadata.getNumberOfHorizontalPads(), requestMessage.getProjectMetadata().numberOfHorizontalPads()) ||
											!Objects.equals(oldMetadata.getNumberOfVerticalPads(), requestMessage.getProjectMetadata().numberOfVerticalPads());

			metadata.setNumberOfHorizontalPads(requestMessage.getProjectMetadata().numberOfHorizontalPads());
			metadata.setNumberOfVerticalPads(requestMessage.getProjectMetadata().numberOfVerticalPads());

			projectService.rename(metadata.getId(), requestMessage.getProjectMetadata().name());
			context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(metadata)));

			if(hasProjectSizeChanged)
			{
				final List<UUID> removedPads = projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);
				removedPads.forEach(projectController::unloadAndRemovePad);

				context.publishEvent(new ProjectUpdate(projectMapper.projectToProjectDto(project)));
				return Optional.of(new UndoItem("", "", requestMessage, new CompoundRequest(List.of(
						new ProjectSettingsUpdateRequest(projectMetadataMapper.projectMetadataToProjectMetadataDto(oldMetadata)),
						new ProjectUpdateRequest(projectMapper.projectToProjectDto(oldProject))
				))));
			}

			return Optional.of(inverseOperation);
		}
		catch(ProjectNameAlreadyExistsException e)
		{
			project.setMetadata(oldMetadata);
			throw e;
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