package de.tobias.playwall.server.api.pad.handler;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playwall.common.api.CompoundRequest;
import de.tobias.playwall.common.api.pad.request.PadDeleteContentRequest;
import de.tobias.playwall.common.api.pad.request.PadNewMediaRequest;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.api.pad.request.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.common.net.RequestMessage;
import de.tobias.playwall.common.utils.FileFormats;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.pad.PadContent;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@RequestHandlerTyped(PadNewMediaRequest.class)
class PadNewMediaHandler implements UndoableRequestHandler<PadNewMediaRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final Executor asyncExecutor;
	private final MessageSource messageSource;

	public PadNewMediaHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor asyncExecutor, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.asyncExecutor = asyncExecutor;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadNewMediaRequest requestMessage) throws PlayWallServerException
	{
		final UndoItem undoItem = getInverseOperation(requestMessage);

		final PadController oldController = projectController.getPadController(requestMessage.getPadId());
		if(oldController != null)
		{
			oldController.unload();
		}
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		final Path path = Paths.get(requestMessage.getPath());
		final PadContent content = switch(FileFormats.getContentTypeForFile(path))
		{
			case AUDIO ->
			{
				if(pad.getContent() != null && pad.getContent() instanceof AudioPadContent audioPadContent)
				{
					audioPadContent.setMediaPath(path.toString());
					yield audioPadContent;
				}
				else
				{
					yield AudioPadContent.builder().mediaPath(path.toString()).build();
				}
			}
		};
		pad.setContent(content);

		final PadController newPadController = projectController.createNewPadController(pad);

		pad.setName(PathUtils.getFilenameWithoutExtension(path.getFileName()));
		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));

		// Load pad async
		asyncExecutor.execute(newPadController::load);

		return Optional.ofNullable(undoItem);
	}

	private UndoItem getInverseOperation(PadNewMediaRequest request)
	{
		final Pad pad = projectController.getPad(request.getPadId());
		if(pad == null)
		{
			return null;
		}

		final String shortDescription = messageSource.getMessage("undo.description.short.pad.new.media", new Object[]{}, LocaleContextHolder.getLocale());
		final String pageName = projectController.getPageByPad(pad.getId()).getName();
		final String longDescription = messageSource.getMessage("undo.description.long.pad.new.media", new Object[]{pad.getPosition() + 1, pageName}, LocaleContextHolder.getLocale());

		if(pad.getContent() == null)
		{
			return new UndoItem(shortDescription, longDescription, request, new PadDeleteContentRequest(pad.getId()));
		}

		final RequestMessage reloadOldMediaRequest = switch(pad.getContent())
		{
			case AudioPadContent audioPadContent -> new PadNewMediaRequest(pad.getId(), audioPadContent.getMediaPath());
		};
		return new UndoItem(shortDescription, longDescription, request, new CompoundRequest(List.of(
				reloadOldMediaRequest,
				new PadSettingsUpdateRequest(pad.getId(), padMapper.padToPadDto(pad)) // Set old pad settings (name, loop, volume, ...)
		)));
	}
}
