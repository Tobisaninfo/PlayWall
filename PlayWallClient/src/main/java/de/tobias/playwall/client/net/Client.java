package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.PadNewMediaResponse;

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

	Page addPage(UUID projectId, String name) throws PlayWallApiException;

	Page renamePage(UUID projectId, UUID pageId, String newName) throws PlayWallApiException;

	void deletePage(UUID projectId, UUID pageId) throws PlayWallApiException;

	Page duplicatePage(UUID projectId, UUID pageId, String name) throws PlayWallApiException;

	void play(UUID padId) throws PlayWallApiException;

	void pause(UUID padId) throws PlayWallApiException;

	void stop(UUID padId) throws PlayWallApiException;

	PadNewMediaResponse newMedia(UUID padId, Path file) throws PlayWallApiException;
}
