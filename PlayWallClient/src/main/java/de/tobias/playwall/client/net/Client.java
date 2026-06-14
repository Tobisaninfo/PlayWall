package de.tobias.playwall.client.net;

import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.page.PageSettings;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.utils.ExportFile;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.Color;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Client
{
	interface ConnectingListener
	{
		void onFailure(int currentTry, int maximumNumberOfTries);
	}

	ReadOnlyObjectProperty<ConnectionState> connectionStateProperty();

	void connect();

	void connectWithRetries(int numberOfRetries, ConnectingListener listener);

	void disconnect();

	AllProjectsInfo getProjects() throws PlayWallApiException;

	ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException;

	void deleteProject(UUID projectId) throws PlayWallApiException;

	Project getProject(UUID projectId) throws PlayWallApiException;

	void loadProject(UUID projectId) throws PlayWallApiException;

	void closeProject() throws PlayWallApiException;

	boolean isSaved() throws PlayWallApiException;

	void saveProject() throws PlayWallApiException;

	void renameProject(UUID projectId, String newName) throws PlayWallApiException;

	UUID duplicateProject(UUID projectId) throws PlayWallApiException;

	ExportFile exportProject(UUID projectId) throws PlayWallApiException;

	UUID importProject(ExportFile projectFile) throws PlayWallApiException;

	void undo() throws PlayWallApiException;

	void redo() throws PlayWallApiException;

	void addPage() throws PlayWallApiException;

	void updatePageSettings(UUID pageId, PageSettings settings) throws PlayWallApiException;

	void deletePage(UUID pageId) throws PlayWallApiException;

	void duplicatePage(UUID pageId) throws PlayWallApiException;

	void reorderPage(Map<UUID, Integer> positions) throws PlayWallApiException;

	ExportFile exportPage(UUID pageId) throws PlayWallApiException;

	void importPage(ExportFile pageFile) throws PlayWallApiException;

	void play(UUID padId) throws PlayWallApiException;

	void pause(UUID padId) throws PlayWallApiException;

	void stop(UUID padId) throws PlayWallApiException;

	void newMedia(UUID padId, Path file) throws PlayWallApiException;

	void updateSettings(UUID padId, Pad pad) throws PlayWallApiException;

	void deletePad(UUID padId) throws PlayWallApiException;

	void duplicatePad(Pad sourcePad, UUID targetPad) throws PlayWallApiException;

	void changeVolume(UUID padId, double volume) throws PlayWallApiException;

	void changeGlobalVolume(double volume) throws PlayWallApiException;

	void updateProjectSettings(ProjectMetadata projectMetadata) throws PlayWallApiException;

	void stopAllPads() throws PlayWallApiException;

	void batchColorPads(Set<UUID> padIds, ModernColor color) throws PlayWallApiException;

	void batchReplaceMedia(Map<UUID, String> newMediaPathsByPadId, Set<UUID> padIdsToDelete) throws PlayWallApiException;

	Settings getProgramSettings() throws PlayWallApiException;

	void updateProgramSettings(Settings settings) throws PlayWallApiException;

	List<AudioDeviceInstance> getOutputDevices() throws PlayWallApiException;

	void playTestSound(String audioDeviceName) throws PlayWallApiException;

	void stopTestSound() throws PlayWallApiException;

	void easterEgg() throws PlayWallApiException;
}
