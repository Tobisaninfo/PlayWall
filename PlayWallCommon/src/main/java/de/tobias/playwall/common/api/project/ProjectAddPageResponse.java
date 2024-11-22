package de.tobias.playwall.common.api.project;

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
	private PageMetadataDto page;

	public ProjectAddPageResponse(UUID messageId, boolean success, PageMetadataDto page)
	{
		super(messageId);
		this.success = success;
		this.page = page;
	}
}
