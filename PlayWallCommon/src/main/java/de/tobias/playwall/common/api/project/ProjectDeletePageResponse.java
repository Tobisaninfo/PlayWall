package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectDeletePageResponse extends ResponseMessage
{
	public ProjectDeletePageResponse(UUID messageId)
	{
		super(messageId);
	}
}
