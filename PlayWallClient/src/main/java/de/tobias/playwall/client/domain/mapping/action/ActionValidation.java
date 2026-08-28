package de.tobias.playwall.client.domain.mapping.action;

import de.tobias.playwall.client.domain.project.Project;

public interface ActionValidation
{
	boolean isValid(Project project);
}
