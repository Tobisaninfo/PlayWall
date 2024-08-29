package de.tobias.playwall.common.net.project;

import de.tobias.playwall.common.net.EventType;

public enum ProjectEventMessageType implements EventType
{
	LIST_PROJECTS_REQUEST,
	LIST_PROJECTS_RESPONSE;

	public enum ListProjectsProperties
	{
		PROJECTS;
	}
}
