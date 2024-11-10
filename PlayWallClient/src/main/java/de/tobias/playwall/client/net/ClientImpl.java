package de.tobias.playwall.client.net;

import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.common.api.project.ProjectDeleteRequest;
import de.tobias.playwall.common.api.project.ProjectDeleteResponse;
import de.tobias.playwall.common.api.project.ProjectListRequest;
import de.tobias.playwall.common.api.project.ProjectListResponse;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ClientImpl implements Client
{
	private final ClientWebSocketHandler clientWebSocketHandler;
	private final ProjectMetadataMapper projectMetadataMapper;

	public ClientImpl(ClientWebSocketHandler clientWebSocketHandler, ProjectMetadataMapper projectMetadataMapper)
	{
		this.clientWebSocketHandler = clientWebSocketHandler;
		this.projectMetadataMapper = projectMetadataMapper;
	}

	@Override
	public void getProjects(Consumer<List<ProjectMetadataDao>> callback)
	{
		clientWebSocketHandler.send(new ProjectListRequest(), (ProjectListResponse res) -> {
			callback.accept(res.getProjects().stream()
					.map(projectMetadataMapper::projectMetadataDtoToProjectMapperDao)
					.sorted(Comparator.comparing(ProjectMetadataDao::name))
					.toList());
		});
	}

	@Override
	public void deleteProject(ProjectMetadataDao mock, Consumer<ProjectDeleteResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(mock.id()), callback);
	}
}
