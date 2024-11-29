package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectRenamePageResponse extends ResponseMessage
{
	private PageDto page;

	public ProjectRenamePageResponse(UUID messageId, PageDto page)
	{
		super(messageId);
		this.page = page;
	}
}
