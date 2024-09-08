package de.tobias.playwall.common.net.project;

import de.tobias.playwall.common.net.BaseMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectDeleteRequest extends BaseMessage
{
	private UUID projectId;

	public ProjectDeleteRequest(UUID messageId, UUID projectId)
	{
		super(messageId);
		this.projectId = projectId;
	}
}
