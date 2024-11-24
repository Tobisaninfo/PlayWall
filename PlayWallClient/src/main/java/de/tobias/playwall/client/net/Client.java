package de.tobias.playwall.client.net;

import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Client
{
	void connect();

	void disconnect();

	void getProjects(Consumer<List<ProjectMetadata>> callback);

	void addProject(String name, BiConsumer<Boolean, ProjectMetadata> callback);

	void deleteProject(ProjectMetadata project, Consumer<ProjectDeleteResponse> callback);
}
