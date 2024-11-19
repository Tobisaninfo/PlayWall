package de.tobias.playwall.client.net;

import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.common.api.project.ProjectAddResponse;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;

import java.util.List;
import java.util.function.Consumer;

public interface Client
{
	void connect();

	void disconnect();

	void getProjects(Consumer<List<ProjectMetadataDao>> callback);

	void addProject(String name, Consumer<ProjectAddResponse> callback);

	void deleteProject(ProjectMetadataDao project, Consumer<ProjectDeleteResponse> callback);
}
