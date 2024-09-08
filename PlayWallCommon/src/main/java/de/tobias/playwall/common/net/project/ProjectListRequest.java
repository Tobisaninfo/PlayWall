package de.tobias.playwall.common.net.project;

import de.tobias.playwall.common.net.BaseMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectListRequest extends BaseMessage
{
	public ProjectListRequest(UUID messageId)
	{
		super(messageId);
	}
}
