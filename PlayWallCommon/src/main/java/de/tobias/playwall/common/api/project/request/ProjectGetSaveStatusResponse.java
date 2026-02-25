package de.tobias.playwall.common.api.project.request;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectGetSaveStatusResponse extends ResponseMessage
{
	private boolean saved;

	public ProjectGetSaveStatusResponse(UUID messageId, boolean saved)
	{
		super(messageId);
		this.saved = saved;
	}
}
