package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.api.project.request.ProjectSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.request.ProjectUpdateRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.common.api.project.update.ProjectUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.project.*;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.project.ProjectMetadata;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequestHandlerTyped(ProjectSettingsUpdateRequest.class)
class ProjectSettingsUpdateHandler extends UndoableRequestHandler<ProjectSettingsUpdateRequest>
{
	private final ProjectController projectController;
	private final ProjectService projectService;

	private final ProjectMetadataMapper projectMetadataMapper;
	private final ProjectMapper projectMapper;
	private final FadeSettingsMapper fadeSettingsMapper;

	private String shortDescription;
	private String longDescription;

	public ProjectSettingsUpdateHandler(MessageSource messageSource, ApplicationContext context, ProjectController projectController, ProjectService projectService, ProjectMetadataMapper projectMetadataMapper, ProjectMapper projectMapper, FadeSettingsMapper fadeSettingsMapper)
	{
		super(messageSource, context);
		this.projectController = projectController;
		this.projectService = projectService;
		this.projectMetadataMapper = projectMetadataMapper;
		this.projectMapper = projectMapper;
		this.fadeSettingsMapper = fadeSettingsMapper;
	}

	@PostConstruct
	void init()
	{
		shortDescription = messageSource.getMessage("undo.description.short.project.settings", new Object[]{}, LocaleContextHolder.getLocale());
		longDescription = messageSource.getMessage("undo.description.long.project.settings", new Object[]{}, LocaleContextHolder.getLocale());
	}

	@Override
	public Optional<UndoItem> handleRequest(ProjectSettingsUpdateRequest requestMessage) throws IOException
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
			metadata.setIntroColor(requestMessage.getProjectMetadata().introColor());
			metadata.setEofWarningTime(requestMessage.getProjectMetadata().eofWarningTime());
			metadata.setFadeSettings(fadeSettingsMapper.fadeSettingsDtoToFadeSettings(requestMessage.getProjectMetadata().fadeSettings()));

			metadata.setNumberOfHorizontalPads(requestMessage.getProjectMetadata().numberOfHorizontalPads());
			metadata.setNumberOfVerticalPads(requestMessage.getProjectMetadata().numberOfVerticalPads());

			metadata.setMidiDevice(requestMessage.getProjectMetadata().midiDevice());
			metadata.setMappings(requestMessage.getProjectMetadata().mappings());
			metadata.setSelectedMapping(requestMessage.getProjectMetadata().selectedMapping());
			metadata.setIsSoloMode(requestMessage.getProjectMetadata().isSoloMode());

			projectService.rename(metadata.getId(), requestMessage.getProjectMetadata().name());
			context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(metadata)));

			final boolean hasProjectSizeChanged = hasProjectSizeChanged(requestMessage, oldMetadata);
			if(hasProjectSizeChanged)
			{
				final List<UUID> removedPads = projectService.updateNumberOfPadsPerRowAndColumn(project, oldMetadata);
				removedPads.forEach(projectController::unloadAndRemovePadController);

				context.publishEvent(new ProjectUpdate(projectMapper.projectToProjectDto(project)));
				return Optional.of(new UndoItem(shortDescription, longDescription, requestMessage, new CompoundRequest(List.of(
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

	private static boolean hasProjectSizeChanged(ProjectSettingsUpdateRequest requestMessage, ProjectMetadata oldMetadata)
	{
		if(!Objects.equals(oldMetadata.getNumberOfHorizontalPads(), requestMessage.getProjectMetadata().numberOfHorizontalPads()))
		{
			return true;
		}

		return !Objects.equals(oldMetadata.getNumberOfVerticalPads(), requestMessage.getProjectMetadata().numberOfVerticalPads());
	}

	private UndoItem getInverseOperation(ProjectSettingsUpdateRequest request)
	{
		final ProjectMetadataDto oldMetadata = projectMetadataMapper.projectMetadataToProjectMetadataDto(projectController.getLoadedProject().getMetadata());
		return new UndoItem(shortDescription, longDescription, request, new ProjectSettingsUpdateRequest(oldMetadata));
	}
}