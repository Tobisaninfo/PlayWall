package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import de.tobias.playwall.common.api.project.PadNewMediaResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface FluentClient
{
	void connect();

	void connectWithRetries(int numberOfRetries, Client.ConnectingListener listener);

	void disconnect();

	ProjectsBuilder projects();

	interface ProjectsBuilder
	{
		List<ProjectMetadata> list() throws PlayWallApiException;

		ProjectMetadata add(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException;
	}

	ProjectBuilder project(UUID projectId);

	interface ProjectBuilder
	{
		void delete() throws PlayWallApiException;

		Project launch() throws PlayWallApiException;

		PagesBuilder pages();

		PageBuilder page(UUID pageId);
	}

	interface PagesBuilder
	{
		Page addPage(String name) throws PlayWallApiException;
	}

	interface PageBuilder
	{
		Page rename(String name) throws PlayWallApiException;

		void delete(String name) throws PlayWallApiException;

		Page duplicate(String name) throws PlayWallApiException;
	}

	interface PadBuilder
	{
		void play() throws PlayWallApiException;

		void pause() throws PlayWallApiException;

		void stop() throws PlayWallApiException;

		PadNewMediaResponse newMedia(Path file) throws PlayWallApiException;
	}

	PadBuilder pad(UUID padId);
}
