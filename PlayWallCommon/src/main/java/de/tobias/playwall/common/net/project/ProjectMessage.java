package de.tobias.playwall.common.net.project;

import de.tobias.playwall.common.net.RequestResponseMessage;
import de.tobias.playwall.common.net.Scope;

public class ProjectMessage extends RequestResponseMessage<ProjectEventMessageType>
{
	public ProjectMessage()
	{
		super();
		setScope(Scope.PROJECT);
	}
}
