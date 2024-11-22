package de.tobias.playwall.client.net;

import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Client
{
	void connect();

	void disconnect();

	void getProjects(Consumer<List<Project>> callback);

	void addProject(String name, BiConsumer<Boolean, Project> callback);

	void deleteProject(Project project, Consumer<ProjectDeleteResponse> callback);
}
