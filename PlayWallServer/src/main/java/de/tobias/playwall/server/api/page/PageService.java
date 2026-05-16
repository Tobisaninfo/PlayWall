package de.tobias.playwall.server.api.page;

import de.tobias.playwall.server.api.page.model.PageExport;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageService
{
	private static final int MINIMUM_VERSION = 1;
	private static final int LATEST_VERSION = 1;

	private final JsonMapper jsonMapper;
	private final MessageSource messageSource;

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

	public Page importPage(Project project, byte[] data, String mimetype)
	{
		final MimeType mimeType = MimeType.valueOf(mimetype);
		if(!mimeType.equals(MimeTypeUtils.APPLICATION_JSON))
		{
			throw new IllegalArgumentException("Unsupported mimetype: " + mimeType);
		}

		final PageExport.PageExportHeader header = parseHeader(data);
		if(!header.getMimetype().equals(PageExport.PLAYWALL_PAGE))
		{
			throw new IllegalArgumentException(messageSource.getMessage("page.import.error.mimetype", new Object[]{}, LocaleContextHolder.getLocale()));
		}
		if(header.getVersion() < MINIMUM_VERSION)
		{
			throw new IllegalArgumentException(messageSource.getMessage("page.import.error.version.too_old", new Object[]{header.getVersion(), MINIMUM_VERSION}, LocaleContextHolder.getLocale()));
		}

		if(header.getVersion() > LATEST_VERSION)
		{
			throw new IllegalArgumentException(messageSource.getMessage("page.import.error.parse_version", new Object[]{}, LocaleContextHolder.getLocale()));
		}

		final PageExport pageExport = jsonMapper.readValue(data, PageExport.class);

		final Page page = pageExport.page();
		// Set new UUIDs
		page.setId(UUID.randomUUID());
		page.getPads().forEach(pad -> pad.setId(UUID.randomUUID()));

		String name = page.getName();
		int copyIndex = 1;
		while(project.containsPageName(name))
		{
			name = messageSource.getMessage("page.name.duplicate", new Object[]{page.getName(), copyIndex}, LocaleContextHolder.getLocale());
			copyIndex++;
		}
		page.setName(name);
		page.setPosition(project.getPages().size());

		project.getPages().add(page);

		return page;
	}

	private PageExport.PageExportHeader parseHeader(byte[] data)
	{
		try
		{
			final JsonNode root = jsonMapper.readTree(data);
			final String versionText = root.path("header").path("VERSION").asString("");
			if(versionText.isEmpty())
			{
				throw new NullPointerException("No version found");
			}
			final int version = Integer.parseInt(versionText);

			final String mimetypeText = root.path("header").path("MIMETYPE").asString("");
			if(mimetypeText.isEmpty())
			{
				throw new NullPointerException("No version found");
			}

			log.debug("Parsing page file with version {} and mimetypeText {}", version, mimetypeText);

			return new PageExport.PageExportHeader(version, mimetypeText);
		}
		catch(Exception e)
		{
			log.debug("Error parsing project file version", e);
			throw new IllegalArgumentException(messageSource.getMessage("page.import.error.parse_version", new Object[]{}, LocaleContextHolder.getLocale()), e);
		}
	}
}
