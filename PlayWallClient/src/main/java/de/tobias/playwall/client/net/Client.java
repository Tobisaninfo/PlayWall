package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface Client
{
	interface ConnectingListener
	{
		void onFailure(int currentTry, int maximumNumberOfTries);
	}

	void connect();

	void connectWithRetries(int numberOfRetries, ConnectingListener listener);

	void disconnect();

	List<ProjectMetadata> getProjects() throws PlayWallApiException;

	ProjectMetadata addProject(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException;

	void deleteProject(UUID projectId) throws PlayWallApiException;

	Project launchProject(UUID projectId) throws PlayWallApiException;

	void saveProject() throws PlayWallApiException;

	Page addPage(String name) throws PlayWallApiException;

	Page renamePage(UUID pageId, String newName) throws PlayWallApiException;

	void deletePage(UUID pageId) throws PlayWallApiException;

	Page duplicatePage(UUID pageId, String name) throws PlayWallApiException;

	void play(UUID padId) throws PlayWallApiException;

	void pause(UUID padId) throws PlayWallApiException;

	void stop(UUID padId) throws PlayWallApiException;

	void newMedia(UUID padId, Path file) throws PlayWallApiException;

	void updateSettings(UUID padId, Pad pad) throws PlayWallApiException;

	void deletePad(UUID padId) throws PlayWallApiException;
}
