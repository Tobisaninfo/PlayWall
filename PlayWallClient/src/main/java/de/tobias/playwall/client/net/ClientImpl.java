package de.tobias.playwall.client.net;

import de.thecodelabs.utils.application.App;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadMapper;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.page.PageSettingsMapper;
import de.tobias.playwall.client.domain.project.*;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.domain.settings.SettingsMapper;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.history.RedoRequest;
import de.tobias.playwall.common.api.history.UndoRequest;
import de.tobias.playwall.common.api.pad.request.*;
import de.tobias.playwall.common.api.page.request.*;
import de.tobias.playwall.common.api.project.request.*;
import de.tobias.playwall.common.api.settings.SettingsGetRequest;
import de.tobias.playwall.common.api.settings.SettingsGetResponse;
import de.tobias.playwall.common.api.settings.SettingsUpdateRequest;
import de.tobias.playwall.common.api.settings.audiodevices.*;
import de.tobias.playwall.common.utils.MapUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static de.tobias.playwall.common.utils.MapUtils.entry;

@Service(superclass = Client.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Slf4j
class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;
	private final ProjectMapper projectMapper;
	private final PageSettingsMapper pageSettingsMapper;
	private final PadMapper padMapper;
	private final SettingsMapper settingsMapper;
	private final AllProjectsInfoMapper allProjectsInfoMapper;
	private final ColorMapper colorMapper;

	private final ClientWebSocketHandler clientWebSocketHandler;
	private final App app;

	@Override
	public ReadOnlyObjectProperty<ConnectionState> connectionStateProperty()
	{
		return clientWebSocketHandler.connectionStateProperty();
	}

	@Override
	public void connect()
	{
		final String clientId = UUID.randomUUID().toString();
		log.info("Connect to server with client id {}", clientId);

		clientWebSocketHandler.connect(MapUtils.create(
				entry("clientId", clientId),
				entry("X-Protocol-Version", app.getInfo().getVersion()))
		);
		log.info("Connected");
	}

	@Override
	public void connectWithRetries(int numberOfRetries, ConnectingListener listener)
	{
		for(int i = 1; i <= numberOfRetries; i++)
		{
			log.info("Connect to server (Attempt: {}/{})", i, numberOfRetries);
			clientWebSocketHandler.setConnectionState(ConnectionState.RECONNECTING);
			try
			{
				connect();
				return;
			}
			catch(ServerRejectedException e)
			{
				log.error("Server rejected the connection: {}", e.getMessage());
				clientWebSocketHandler.setConnectionState(ConnectionState.DISCONNECTED);
				throw e;
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
	public void closeProject() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectCloseRequest());
	}

	@Override
	public boolean isSaved() throws PlayWallApiException
	{
		final ProjectGetSaveStatusResponse response = clientWebSocketHandler.send(new ProjectGetSaveStatusRequest());
		return response.isSaved();
	}

	@Override
	public void saveProject() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectSaveRequest());
	}

	@Override
	public void renameProject(UUID projectId, String newName) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectRenameRequest(projectId, newName));
	}

	@Override
	public UUID duplicateProject(UUID projectId) throws PlayWallApiException
	{
		final ProjectDuplicateResponse responseMessage = clientWebSocketHandler.send(new ProjectDuplicateRequest(projectId));
		return responseMessage.getProjectId();
	}

	@Override
	public ExportFile exportProject(UUID projectId) throws PlayWallApiException
	{
		final ProjectExportResponse responseMessage = clientWebSocketHandler.send(new ProjectExportRequest(projectId));
		return new ExportFile(responseMessage.getMimetype(), Base64.getDecoder().decode(responseMessage.getBase64()));
	}

	@Override
	public UUID importProject(ExportFile projectFile) throws PlayWallApiException
	{
		final ProjectImportResponse responseMessage = clientWebSocketHandler.send(new ProjectImportRequest(projectFile.mimetype(), Base64.getEncoder().encodeToString(projectFile.data())));
		return responseMessage.getProjectId();
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
	public void updatePageSettings(UUID pageId, PageSettings pageSettings) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageSettingsUpdateRequest(pageId, pageSettingsMapper.pageSettingsToPageSettingsDto(pageSettings)));
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
	public ExportFile exportPage(UUID pageId) throws PlayWallApiException
	{
		final PageExportResponse responseMessage = clientWebSocketHandler.send(new PageExportRequest(pageId));
		return new ExportFile(responseMessage.getMimetype(), Base64.getDecoder().decode(responseMessage.getBase64()));
	}

	@Override
	public void importPage(ExportFile pageFile) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PageImportRequest(pageFile.mimetype(), Base64.getEncoder().encodeToString(pageFile.data())));
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
		clientWebSocketHandler.send(new PadStopRequest(padId, false));
	}

	@Override
	public void stopImmediately(UUID padId) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadStopRequest(padId, true));
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
	public void duplicatePad(UUID sourcePad, UUID targetPad) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadDragDuplicateRequest(sourcePad, targetPad));
	}

	@Override
	public void movePad(UUID sourcePad, UUID targetPad) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadDragMoveRequest(sourcePad, targetPad));
	}

	@Override
	public void swapPad(UUID padId1, UUID padId2) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadDragSwapRequest(padId1, padId2));
	}

	@Override
	public void changeVolume(UUID padId, double volume) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new PadChangeVolumeRequest(padId, volume));
	}

	@Override
	public void changeGlobalVolume(double volume) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new GlobalChangeVolumeRequest(volume));
	}

	@Override
	public void updateProjectSettings(ProjectMetadata projectMetadata) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectSettingsUpdateRequest(projectMetadataMapper.projectMetadataToProjectMetadataDto(projectMetadata)));
	}

	@Override
	public void stopAllPads() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new AllPadsStopRequest(false));
	}

	@Override
	public void stopAllPadsImmediately() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new AllPadsStopRequest(true));
	}

	@Override
	public void showPage(int index) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new ProjectPageShowRequest(index));
	}

	@Override
	public void batchColorPads(Set<UUID> padIds, ModernColor color) throws PlayWallApiException
	{
		final Color mappedColor = colorMapper.modernColorToColor(color);
		final Map<UUID, Color> padColors = padIds.stream().collect(Collectors.toMap(
				id -> id,
				_ -> mappedColor
		));

		clientWebSocketHandler.send(new BatchColorPadsRequest(padColors));
	}

	@Override
	public void batchReplaceMedia(Map<UUID, String> newMediaPathsByPadId, Set<UUID> padIdsToDelete) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new BatchReplaceMediaRequest(newMediaPathsByPadId, padIdsToDelete));
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

	@Override
	public List<AudioDeviceInstance> getOutputDevices() throws PlayWallApiException
	{
		final AudioDevicesGetResponse response = clientWebSocketHandler.send(new AudioDevicesGetRequest());
		return response.getAudioDevices();
	}

	@Override
	public void playTestSound(String audioDeviceName) throws PlayWallApiException
	{
		clientWebSocketHandler.send(new TestSoundPlayRequest(audioDeviceName));
	}

	@Override
	public void stopTestSound() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new TestSoundStopRequest());
	}

	@Override
	public void easterEgg() throws PlayWallApiException
	{
		clientWebSocketHandler.send(new EasterEggRequest());
	}
}
