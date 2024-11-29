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
public class ProjectDeletePageResponse extends ResponseMessage
{
	public ProjectDeletePageResponse(UUID messageId)
	{
		super(messageId);
	}
}
