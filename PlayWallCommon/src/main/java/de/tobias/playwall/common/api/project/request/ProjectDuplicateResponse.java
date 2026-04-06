package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectDuplicateResponse extends ResponseMessage
{
	private UUID projectId;

	public ProjectDuplicateResponse(UUID messageId, UUID projectId)
	{
		super(messageId);
		this.projectId = projectId;
	}
}
