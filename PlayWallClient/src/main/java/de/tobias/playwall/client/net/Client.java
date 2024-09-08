package de.tobias.playwall.client.net;

import de.tobias.playwall.client.project.ProjectReferenceMock;
import de.tobias.playwall.common.net.project.ProjectDeleteResponse;

import java.util.List;
import java.util.function.Consumer;

public interface Client
{
	void getProjects(Consumer<List<ProjectReferenceMock>> callback);

	void deleteProject(ProjectReferenceMock mock, Consumer<ProjectDeleteResponse> callback);
}
