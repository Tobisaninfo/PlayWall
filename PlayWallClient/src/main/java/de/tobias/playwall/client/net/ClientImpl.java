package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.utils.MapUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
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
	public void getProjects(Consumer<List<ProjectMetadata>> callback)
	{
		clientWebSocketHandler.send(new ProjectListRequest(), (ProjectListResponse res) -> {
			callback.accept(res.getProjects().stream()
					.map(projectMetadataMapper::projectMetadataDtoToProjectMetadata)
					.sorted(Comparator.comparing(ProjectMetadata::name))
					.toList());
		});
	}

	@Override
	public void addProject(String name, BiConsumer<Boolean, ProjectMetadata> callback)
	{
		clientWebSocketHandler.send(new ProjectAddRequest(name), (ProjectAddResponse res) ->
				callback.accept(res.isSuccess(), projectMetadataMapper.projectMetadataDtoToProjectMetadata(res.getProject())));
	}

	@Override
	public void deleteProject(ProjectMetadata project, Consumer<ProjectDeleteResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(project.id()), callback);
	}
}
