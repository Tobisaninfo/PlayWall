package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectImportRequest;
import de.tobias.playwall.common.api.project.request.ProjectImportResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RequestHandlerTyped(ProjectImportRequest.class)
class ProjectImportHandler implements GetRequestHandler<ProjectImportRequest>
{
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectImportRequest requestMessage) throws IOException
	{
		final byte[] data = Base64.getDecoder().decode(requestMessage.getBase64());
		final UUID projectId = projectService.importProject(requestMessage.getMimetype(), data);
		return Optional.of(new ProjectImportResponse(requestMessage.getMessageId(), projectId));
	}
}