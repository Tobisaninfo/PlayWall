package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.request.AllPadsStopRequest;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequestHandlerTyped(AllPadsStopRequest.class)
@RequiredArgsConstructor
class AllPadsStopHandler implements OneTimeActionRequestHandler<AllPadsStopRequest>
{
	private final ProjectController projectController;

	@Override
	public void handleRequest(AllPadsStopRequest requestMessage) throws IOException
	{
		projectController.stopAll();
	}
}