package de.tobias.playwall.client.net;

import de.tobias.playwall.client.project.ProjectReferenceMock;
import de.tobias.playwall.common.net.project.ProjectDeleteRequest;
import de.tobias.playwall.common.net.project.ProjectDeleteResponse;
import de.tobias.playwall.common.net.project.ProjectListRequest;
import de.tobias.playwall.common.net.project.ProjectListResponse;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ClientImpl implements Client {

	public ClientImpl(ClientWebSocketHandler clientWebSocketHandler)
	{
		this.clientWebSocketHandler = clientWebSocketHandler;
	}

	private final ClientWebSocketHandler clientWebSocketHandler;

	@Override
	public void getProjects(Consumer<List<ProjectReferenceMock>> callback)
	{
		clientWebSocketHandler.send(new ProjectListRequest(), (ProjectListResponse res) -> {
			callback.accept(res.getProjects().stream()
					.map(s -> new ProjectReferenceMock(s.id(), s.name()))
					.sorted(Comparator.comparing(ProjectReferenceMock::getName))
					.toList());
		});
	}

	@Override
	public void deleteProject(ProjectReferenceMock mock, Consumer<ProjectDeleteResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(mock.getId()), callback);
	}
}
