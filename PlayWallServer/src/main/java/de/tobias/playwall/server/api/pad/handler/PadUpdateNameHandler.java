package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.PadUpdateNameRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;

@RequestHandlerTyped(PadUpdateNameRequest.class)
class PadUpdateNameHandler implements OneTimeActionRequestHandler<PadUpdateNameRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	public PadUpdateNameHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
	}

	@Override
	public void handleRequest(PadUpdateNameRequest requestMessage)
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		pad.setName(requestMessage.getName());

		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));
	}
}
