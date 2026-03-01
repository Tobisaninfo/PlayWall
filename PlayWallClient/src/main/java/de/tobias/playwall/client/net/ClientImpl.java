package de.tobias.playwall.client.net;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.domain.settings.SettingsMapper;
import de.tobias.playwall.common.api.history.RedoRequest;
import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.api.pad.request.*;
import de.tobias.playwall.common.api.page.request.*;
import de.tobias.playwall.common.api.project.request.*;
import de.tobias.playwall.common.api.settings.SettingsGetRequest;
import de.tobias.playwall.common.api.settings.SettingsGetResponse;
import de.tobias.playwall.common.api.settings.SettingsUpdateRequest;
import de.tobias.playwall.common.utils.MapUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static de.tobias.playwall.common.utils.MapUtils.entry;

@Service(superclass = Client.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Slf4j
class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final ProjectMapper projectMapper;
	private final PadMapper padMapper;
	private final SettingsMapper settingsMapper;
	private final AllProjectsInfoMapper allProjectsInfoMapper;

	private final ClientWebSocketHandler clientWebSocketHandler;

	@Override
	public void connect()
	{
		final String clientId = UUID.randomUUID().toString();
		log.info("Connect to server with client id {}", clientId);

		clientWebSocketHandler.connect(MapUtils.create(entry("clientId", clientId)));
		log.info("Connected");
	}

	@Override
	public void connectWithRetries(int numberOfRetries, ConnectingListener listener)
	{
		for(int i = 1; i <= numberOfRetries; i++)
		{
			log.info("Connect to server (Attempt: {}/{})", i, numberOfRetries);
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

				log.error("Failed to connect to the PlayWall server", e);
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
	public AllProjectsInfo getProjects() throws PlayWallApiException
	{
		final ProjectListResponse response = clientWebSocketHandler.send(new ProjectListRequest());
		return allProjectsInfoMapper.allProjectsInfoDtoToAllProjectsInfo(response.getAllProjectsInfo());
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
	public void undo() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new UndoRequest());
	}

	@Override
	public void redo() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new RedoRequest());
	}

	@Override
	public void addPage() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageAddRequest());
	}

	@Override
	public void renamePage(UUID pageId, String newName) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageRenameRequest(pageId, newName));
	}

	@Override
	public void deletePage(UUID pageId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageDeleteRequest(pageId));
	}

	@Override
	public void duplicatePage(UUID pageId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageDuplicateRequest(pageId));
	}

	@Override
	public void reorderPage(Map<UUID, Integer> positions) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageReorderRequest(positions));
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

	@Override
	public void stopAllPads() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new AllPadsStopRequest());
	}

	@Override
	public Settings getProgramSettings() throws PlayWallApiException
	{
		final SettingsGetResponse response = clientWebSocketHandler.send(new SettingsGetRequest());
		return settingsMapper.settingsDtoToSettings(response.getSettings());
	}

	@Override
	public void updateProgramSettings(Settings settings) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new SettingsUpdateRequest(settingsMapper.settingToSettingsDto(settings)));
	}
}
