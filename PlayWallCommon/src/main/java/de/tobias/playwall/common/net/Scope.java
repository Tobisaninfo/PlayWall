package de.tobias.playwall.common.net;

import de.tobias.playwall.common.net.project.ProjectMessage;

public enum Scope
{
	PROJECT(ProjectMessage.class);

	private final Class<? extends Message> clazz;

	Scope(Class<? extends Message> clazz)
	{
		this.clazz = clazz;
	}

	public Class<? extends Message> getClazz()
	{
		return clazz;
	}
}
