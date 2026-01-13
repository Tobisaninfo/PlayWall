package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.mapper.PadMapper;
import de.tobias.playwall.client.mapper.PageMapper;
import de.tobias.playwall.client.mapper.ProjectMapper;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.api.project.model.ProjectMetadataDto;
import de.tobias.playwall.common.utils.MapUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static de.tobias.playwall.common.utils.MapUtils.entry;

@Service(superclass = Client.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final ProjectMapper projectMapper;
	private final PageMapper pageMapper;
	private final PadMapper padMapper;

	private final ClientWebSocketHandler clientWebSocketHandler;

	@Override
	public void connect()
	{
		final String clientId = UUID.randomUUID().toString();
		Logger.info("Connect to server with client id {0}", clientId);

		clientWebSocketHandler.connect(MapUtils.create(entry("clientId", clientId)));
		Logger.info("Connected");
	}

	@Override
	public void connectWithRetries(int numberOfRetries, ConnectingListener listener)
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
				listener.onFailure(i, numberOfRetries);
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
				.sorted(Comparator.comparing(ProjectMetadata::getName))
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
	public Project getProject(UUID projectId) throws PlayWallApiException
	{
		final ProjectGetResponse response = clientWebSocketHandler.send(new ProjectGetRequest(projectId));
		return projectMapper.projectDtoToProject(response.getProject());
	}

	@Override
	public void loadProject(UUID projectId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectLoadRequest(projectId));
	}

	@Override
	public void saveProject() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectSaveRequest());
	}

	@Override
	public Page addPage(String name) throws PlayWallApiException
	{
		final PageAddResponse response = clientWebSocketHandler.send(new PageAddRequest(name));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public Page renamePage(UUID pageId, String newName) throws PlayWallApiException
	{
		final ProjectRenamePageResponse response = clientWebSocketHandler.send(new PageRenameRequest(pageId, newName));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public void deletePage(UUID pageId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageDeleteRequest(pageId));
	}

	@Override
	public Page duplicatePage(UUID pageId, String name) throws PlayWallApiException
	{
		final ProjectDuplicatePageResponse response = clientWebSocketHandler.send(new PageDuplicateRequest(pageId, name));
		return pageMapper.pageDtoToPage(response.getPage());
	}

	@Override
	public void play(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadPlayRequest(padId));
	}

	@Override
	public void pause(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadPauseRequest(padId));
	}

	@Override
	public void stop(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadStopRequest(padId));
	}

	@Override
	public void newMedia(UUID padId, Path file) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadNewMediaRequest(padId, file.toAbsolutePath().toString()));
	}

	@Override
	public void updateSettings(UUID padId, Pad pad) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadSettingsUpdateRequest(padId, padMapper.padToPadDto(pad)));
	}

	@Override
	public void deletePad(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadDeleteContentRequest(padId));
	}

	@Override
	public void changeVolume(UUID padId, double volume) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadChangeVolumeRequest(padId, volume));
	}

	@Override
	public void changeGlobalVolume(double volume) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new GlobaleChangeVolumeRequest(volume));
	}

	@Override
	public void updateProjectSettings(ProjectMetadata projectMetadata) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectSettingsUpdateRequest(projectMetadataMapper.projectMetadataToProjectMetadataDto(projectMetadata)));
	}
}
