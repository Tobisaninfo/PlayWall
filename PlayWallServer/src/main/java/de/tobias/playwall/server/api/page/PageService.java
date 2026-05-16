package de.tobias.playwall.server.api.page;

import de.tobias.playwall.server.api.page.model.PageExport;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService
{
	private final JsonMapper jsonMapper;

	public byte[] exportPage(Project project, UUID pageId)
	{
		final Optional<Page> pageOptional = project.getPageById(pageId);
		if(pageOptional.isEmpty())
		{
			throw new PageNotExistsException(project.getMetadata().getId(), pageId);
		}

		final Page page = pageOptional.get();
		return jsonMapper.writeValueAsBytes(new PageExport(page));
	}
}
