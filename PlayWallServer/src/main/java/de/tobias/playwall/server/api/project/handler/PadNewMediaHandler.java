package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.utils.FileFormats;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Executor;

@RequestHandlerTyped(PadNewMediaRequest.class)
public class PadNewMediaHandler implements RequestHandler<PadNewMediaRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final Executor asyncExecutor;

	public PadNewMediaHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor asyncExecutor)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.asyncExecutor = asyncExecutor;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(PadNewMediaRequest requestMessage) throws IOException, PlayWallServerException
	{
		final PadController oldController = projectController.getPadController(requestMessage.getPadId());
		if(oldController != null)
		{
			oldController.stop();
			oldController.unload();
		}
		final Pad pad = projectController.getPad(requestMessage.getPadId());

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

		return Optional.empty();
	}
}
