package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadContentDto;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PadMapper;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.PadContent;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.Executor;

@RequestHandlerTyped(PadSettingsUpdateRequest.class)
public class PadSettingsUpdateHandler implements RequestHandler<PadSettingsUpdateRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final Executor asyncExecutor;
	private final MessageSource messageSource;

	public PadSettingsUpdateHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor asyncExecutor, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.asyncExecutor = asyncExecutor;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(PadSettingsUpdateRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		pad.setName(requestMessage.getPad().getName());

		updatePadContent(requestMessage.getPad().getContent(), pad);

		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));

		return Optional.empty();
	}

	private void updatePadContent(PadContentDto requestPadContent, Pad pad)
	{
		final PadContent padContentToUpdate = pad.getContent();

		if(requestPadContent == null)
		{
			return;
		}

		if(padContentToUpdate == null)
		{
			return;
		}

		switch(padContentToUpdate)
		{
			case AudioPadContent audioPadContent ->
			{
				if(!AudioPadContentDto.class.isAssignableFrom(requestPadContent.getClass()))
				{
					throw new RuntimeException(MessageFormat.format("Invalid pad content class. Expected: {0}, actual: {1}", pad.getContent().getClass(), AudioPadContentDto.class));
				}

				final AudioPadContentDto requestAudioPadContent = (AudioPadContentDto) requestPadContent;
				audioPadContent.setLoop(requestAudioPadContent.isLoop());
				handleVolume(pad, audioPadContent, requestAudioPadContent);

				final String newPath = requestAudioPadContent.getMediaPath();

				if(!audioPadContent.getMediaPath().equals(newPath))
				{
					handleNewMediaPath(pad, audioPadContent, newPath);
				}
			}
		}
	}

	private void handleNewMediaPath(Pad pad, AudioPadContent audioPadContent, String newPath)
	{
		final PadController oldController = projectController.getPadController(pad.getId());
		if(oldController != null)
		{
			oldController.stop();
			oldController.unload();
		}

		audioPadContent.setMediaPath(newPath);

		if(newPath == null)
		{
			pad.setContent(null);
			return;
		}

		final PadController newPadController = projectController.createNewPadController(pad);

		pad.setName(PathUtils.getFilenameWithoutExtension(Paths.get(newPath).getFileName()));

		// Load pad async
		asyncExecutor.execute(newPadController::load);
	}

	private void handleVolume(Pad pad, AudioPadContent audioPadContent, AudioPadContentDto requestAudioPadContent)
	{
		audioPadContent.setVolume(requestAudioPadContent.getVolume());

		final PadController padController = projectController.getPadController(pad.getId());
		if(padController != null)
		{
			padController.setVolume(requestAudioPadContent.getVolume());
		}
	}
}
