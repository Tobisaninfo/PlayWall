package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadPauseRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PadPauseRequest.class)
public class PadPauseHandler implements RequestHandler<PadPauseRequest>
{
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(PadPauseRequest requestMessage) throws IOException, PlayWallServerException
	{
		final PadController controller = projectController.getPadController(requestMessage.getPadId());
		if(controller == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getPadId()), error);
		}

		controller.pause();

		return Optional.empty();
	}
}
