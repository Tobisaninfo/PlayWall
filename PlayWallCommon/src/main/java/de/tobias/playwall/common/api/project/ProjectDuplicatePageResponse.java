package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.project.model.PageDto;
import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectDuplicatePageResponse extends ResponseMessage
{
	private PageDto page;

	public ProjectDuplicatePageResponse(UUID messageId, PageDto page)
	{
		super(messageId);
		this.page = page;
	}
}
