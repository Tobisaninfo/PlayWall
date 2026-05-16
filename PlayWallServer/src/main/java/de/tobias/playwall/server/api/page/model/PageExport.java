package de.tobias.playwall.server.api.page.model;

import de.tobias.playwall.server.common.model.page.Page;
import lombok.*;

public record PageExport(PageExportHeader header, Page page)
{
	public PageExport(Page page)
	{
		this(new PageExportHeader(), page);
	}

	@Getter
	@Setter
	@ToString
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor
	static class PageExportHeader
	{
		private final int VERSION = 1;
		private final String MIMETYPE = "playwall/page";
	}

}
