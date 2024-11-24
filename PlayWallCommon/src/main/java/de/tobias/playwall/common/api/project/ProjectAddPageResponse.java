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
public class ProjectAddPageResponse extends ResponseMessage
{
	private boolean success;
	private PageDto page;

	public ProjectAddPageResponse(UUID messageId, boolean success, PageDto page)
	{
		super(messageId);
		this.success = success;
		this.page = page;
	}
}
