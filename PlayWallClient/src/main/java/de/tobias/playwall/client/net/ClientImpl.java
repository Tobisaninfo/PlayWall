package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.ProjectMetadataDao;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.utils.MapUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static de.tobias.playwall.common.utils.MapUtils.entry;

public class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;

	private ClientWebSocketHandler clientWebSocketHandler;

	public ClientImpl(ProjectMetadataMapper projectMetadataMapper)
	{
		this.projectMetadataMapper = projectMetadataMapper;
	}

	@Override
	public void connect()
	{
		final String clientId = UUID.randomUUID().toString();
		Logger.info("Connect to server with client id {0}", clientId);

		clientWebSocketHandler = new ClientWebSocketHandler();
		clientWebSocketHandler.connect(MapUtils.create(entry("clientId", clientId)));
		Logger.info("Connected");
	}

	@Override
	public void disconnect()
	{
		clientWebSocketHandler.disconnect();
	}

	@Override
	public void getProjects(Consumer<List<ProjectMetadataDao>> callback)
	{
		clientWebSocketHandler.send(new ProjectListRequest(), (ProjectListResponse res) -> {
			callback.accept(res.getProjects().stream()
					.map(projectMetadataMapper::projectMetadataDtoToProjectMetadataDao)
					.sorted(Comparator.comparing(ProjectMetadataDao::name))
					.toList());
		});
	}

	@Override
	public void addProject(String name, Consumer<ProjectAddResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectAddRequest(name), callback);
	}

	@Override
	public void deleteProject(ProjectMetadataDao project, Consumer<ProjectDeleteResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(project.id()), callback);
	}
}
