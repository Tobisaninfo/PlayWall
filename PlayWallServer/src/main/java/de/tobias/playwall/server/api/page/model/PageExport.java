package de.tobias.playwall.server.api.page.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.tobias.playwall.server.common.model.page.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public record PageExport(PageExportHeader header, Page page)
{
	public static final String PLAYWALL_PAGE = "playwall/page";

	public PageExport(Page page)
	{
		this(new PageExportHeader(), page);
	}

	@Getter
	@Setter
	@ToString
	@AllArgsConstructor
	public static class PageExportHeader
	{
		@JsonProperty("VERSION")
		private final int version;
		@JsonProperty("MIMETYPE")
		private final String mimetype;

		PageExportHeader()
		{
			version = 1;
			mimetype = PLAYWALL_PAGE;
		}
	}

}
