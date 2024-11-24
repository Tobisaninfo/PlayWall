package de.tobias.playwall.client.net;

import de.tobias.playwall.client.model.project.ProjectMetadata;

import java.util.List;

public interface Client
{
	void connect();

	void disconnect();

	List<ProjectMetadata> getProjects();

	ProjectMetadata addProject(String name);

	void deleteProject(ProjectMetadata project);
}
