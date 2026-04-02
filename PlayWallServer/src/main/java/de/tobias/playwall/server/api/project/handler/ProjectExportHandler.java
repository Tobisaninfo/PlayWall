package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectExportRequest;
import de.tobias.playwall.common.api.project.request.ProjectExportResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.ProjectService;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectExportRequest.class)
class ProjectExportHandler implements GetRequestHandler<ProjectExportRequest>
{
	private final ProjectService projectService;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectExportRequest requestMessage) throws IOException
	{
		final byte[] projectFile = projectService.exportProject(requestMessage.getProjectId());
		return Optional.of(new ProjectExportResponse(requestMessage.getMessageId(), MimeTypeUtils.APPLICATION_JSON_VALUE, Base64.getEncoder().encodeToString(projectFile)));
	}
}