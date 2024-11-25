package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.utils.MapUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
	public List<ProjectMetadata> getProjects() throws PlayWallApiException
	{
		final ProjectListResponse response = clientWebSocketHandler.send(new ProjectListRequest());
		return response.getProjects().stream()
				.map(projectMetadataMapper::projectMetadataDtoToProjectMetadata)
				.sorted(Comparator.comparing(ProjectMetadata::name))
				.toList();
	}

	@Override
	public ProjectMetadata addProject(String name) throws PlayWallApiException
	{
		final ProjectAddResponse response = clientWebSocketHandler.send(new ProjectAddRequest(name));
		return projectMetadataMapper.projectMetadataDtoToProjectMetadata(response.getProject());
	}

	@Override
	public void deleteProject(ProjectMetadata project) throws PlayWallApiException
	{
		final ProjectDeleteResponse response = clientWebSocketHandler.send(new ProjectDeleteRequest(project.id()));
		if(!response.isSuccess())
		{
			throw new RuntimeException("Failed to delete project " + project.name());
		}
	}
}
