package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.AllPadsStopRequest;
import de.tobias.playwall.common.api.project.ProjectNotLoadedError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(AllPadsStopRequest.class)
@RequiredArgsConstructor
class AllPadsStopHandler implements RequestHandler<AllPadsStopRequest>
{
	private final ProjectController projectController;
	private final MessageSource messageSource;

	@Override
	public Optional<ResponseMessage> handleRequest(AllPadsStopRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Project project = projectController.getLoadedProject();
		if(project == null)
		{
			final ProjectNotLoadedError error = new ProjectNotLoadedError();
			throw new PlayWallServerException(messageSource, error);
		}

		projectController.getPlayingPadControllers().forEach(PadController::stop);

		return Optional.empty();
	}
}