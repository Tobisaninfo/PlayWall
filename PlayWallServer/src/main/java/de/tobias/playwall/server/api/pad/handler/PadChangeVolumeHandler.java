package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadChangeVolumeRequest;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.MessageSource;

import java.io.IOException;

@RequestHandlerTyped(PadChangeVolumeRequest.class)
class PadChangeVolumeHandler implements OneTimeActionRequestHandler<PadChangeVolumeRequest>
{
	private final ProjectController projectController;

	private final MessageSource messageSource;

	public PadChangeVolumeHandler(ProjectController projectController, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.messageSource = messageSource;
	}

	@Override
	public void handleRequest(PadChangeVolumeRequest requestMessage) throws IOException, PlayWallServerException
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
	}
}
