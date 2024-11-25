package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.ProjectMetadata;

import java.util.List;
import java.util.UUID;

public interface Client
{
	void connect();

	void disconnect();

	List<ProjectMetadata> getProjects() throws PlayWallApiException;

	ProjectMetadata addProject(String name) throws PlayWallApiException;

	void deleteProject(UUID projectId) throws PlayWallApiException;

	Page addPage(UUID projectId, String name) throws PlayWallApiException;
}
