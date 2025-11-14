package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.mapper.PageMapper;
import de.tobias.playwall.client.mapper.ProjectMapper;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.utils.MapUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static de.tobias.playwall.common.utils.MapUtils.entry;

@Service(superclass = Client.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final ProjectMapper projectMapper;
	private final PageMapper pageMapper;

	private ClientWebSocketHandler clientWebSocketHandler;

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
	public void connectWithRetries(int numberOfRetries)
	{
		for(int i = 1; i <= numberOfRetries; i++)
		{
			Logger.info("Connect to server (Attempt: {0}/{1})", i, numberOfRetries);
			try
			{
				connect();
				return;
			}
			catch(Exception e)
			{
				if(i == numberOfRetries)
				{
					throw e;
				}

				Logger.error("Failed to connect to the PlayWall server", e);
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException _)
				{
					Thread.currentThread().interrupt();
					throw new ServerConnectionException(e);
				}
			}
		}

		throw new ServerConnectionException("Could not connect to server");
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
	public ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException
	{
		final ProjectAddResponse response = clientWebSocketHandler.send(new ProjectAddRequest(name, numberOfHorizontalPads, numberOfVerticalPads));
		return projectMetadataMapper.projectMetadataDtoToProjectMetadata(response.getProject());
	}

	@Override
	public void deleteProject(UUID projectId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(projectId));
	}

	@Override
	public Project launchProject(UUID projectId) throws PlayWallApiException
	{
		final ProjectLaunchResponse response = clientWebSocketHandler.send(new ProjectLaunchRequest(projectId));
		return projectMapper.projectDtoToProject(response.getProject());
	}

	@Override
	public Page addPage(UUID projectId, String name) throws PlayWallApiException
	{
		final ProjectAddPageResponse response = clientWebSocketHandler.send(new ProjectAddPageRequest(projectId, name));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public Page renamePage(UUID projectId, UUID pageId, String newName) throws PlayWallApiException
	{
		final ProjectRenamePageResponse response = clientWebSocketHandler.send(new ProjectRenamePageRequest(projectId, pageId, newName));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public void deletePage(UUID projectId, UUID pageId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectDeletePageRequest(projectId, pageId));
	}

	@Override
	public Page duplicatePage(UUID projectId, UUID pageId, String name) throws PlayWallApiException
	{
		final ProjectDuplicatePageResponse response = clientWebSocketHandler.send(new ProjectDuplicatePageRequest(projectId, pageId, name));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public void play(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadPlayRequest(padId));
	}
}
