package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.io.PathUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadNewMediaResponse;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PadNewMediaRequest.class)
public class PadNewMediaHandler implements RequestHandler<PadNewMediaRequest>
{
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(PadNewMediaRequest requestMessage) throws IOException, PlayWallServerException
	{
		final PadController controller = projectController.getPadController(requestMessage.getPadId());
		if(controller == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(Localization.getString(error.getLocalizationKey(), requestMessage.getPadId()), error);
		}

		controller.stop();
		controller.unload();

		final Pad pad = controller.getPad();
		((AudioPadContent) pad.getContent()).setMediaPath(requestMessage.getPath()); // TODO: Decision strategy
		controller.load();

		pad.setName(PathUtils.getFilenameWithoutExtension(Paths.get(requestMessage.getPath()).getFileName()));

		return Optional.of(new PadNewMediaResponse(requestMessage.getMessageId(), pad.getId(), pad.getName()));
	}
}
