package de.tobias.playwall.server.api.page.handler;

import de.tobias.playwall.common.api.page.request.PageExportRequest;
import de.tobias.playwall.common.api.page.request.PageExportResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.page.PageService;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PageExportRequest.class)
class PageExportHandler implements GetRequestHandler<PageExportRequest>
{
	private final ProjectController projectController;
	private final PageService pageService;

	@Override
	public Optional<ResponseMessage> handleRequest(PageExportRequest requestMessage) throws IOException
	{
		final Project project = projectController.getLoadedProject();
		final byte[] page = pageService.exportPage(project, requestMessage.getPageId());
		return Optional.of(new PageExportResponse(requestMessage.getMessageId(), MimeTypeUtils.APPLICATION_JSON_VALUE, Base64.getEncoder().encodeToString(page)));
	}
}