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
public class ProjectDeleteResponse extends ResponseMessage
{
	private boolean success;

	public ProjectDeleteResponse(UUID messageId, boolean success)
	{
		super(messageId);
		this.success = success;
	}
}
