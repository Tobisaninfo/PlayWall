package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.ProjectAddPageRequest;
import de.tobias.playwall.common.api.project.ProjectAddPageResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.project.PageMetadata;
import de.tobias.playwall.server.api.project.PageMetadataMapper;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(ProjectAddPageRequest.class)
public class ProjectAddPageHandler implements RequestHandler<ProjectAddPageRequest>
{
	private final ProjectRepository projectRepository;
	private final PageMetadataMapper mapper;

	@Override
	public Optional<ResponseMessage> handleRequest(ProjectAddPageRequest requestMessage) throws IOException
	{
		final Optional<PageMetadata> pageOptional = projectRepository.addPage(requestMessage.getProjectId(), requestMessage.getName());
		if(pageOptional.isPresent())
		{
			return Optional.of(new ProjectAddPageResponse(requestMessage.getMessageId(), true, mapper.pageMetadataToPageMetadataDto(pageOptional.get())));
		}
		else
		{
			// TODO: handle project does not exist or general error
			return Optional.empty();
		}
	}
}
