package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadChangeVolumeRequest;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;

import java.io.IOException;

@RequestHandlerTyped(PadChangeVolumeRequest.class)
class PadChangeVolumeHandler implements OneTimeActionRequestHandler<PadChangeVolumeRequest>
{
	private final ProjectController projectController;

	public PadChangeVolumeHandler(ProjectController projectController)
	{
		this.projectController = projectController;
	}

	@Override
	public void handleRequest(PadChangeVolumeRequest requestMessage) throws IOException
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());

		VolumeHelper.validateVolume(requestMessage.getVolume());

		final PadController padController = projectController.getPadController(pad.getId());
		if(padController != null)
		{
			padController.setVolume(projectController.getLoadedProject().getMetadata().getVolume() * requestMessage.getVolume());
		}
	}
}
