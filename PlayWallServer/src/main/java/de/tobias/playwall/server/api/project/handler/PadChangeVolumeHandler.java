package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadChangeVolumeRequest;
import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.PadContent;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PadChangeVolumeRequest.class)
public class PadChangeVolumeHandler implements RequestHandler<PadChangeVolumeRequest>
{
	private final ProjectController projectController;

	private final MessageSource messageSource;

	public PadChangeVolumeHandler(ProjectController projectController, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<ResponseMessage> handleRequest(PadChangeVolumeRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		VolumeHelper.validateVolume(requestMessage.getVolume());

		final PadController padController = projectController.getPadController(pad.getId());
		if(padController != null)
		{
			padController.setVolume(projectController.getLoadedProject().getMetadata().getVolume() * requestMessage.getVolume());
		}

		return Optional.empty();
	}
}
