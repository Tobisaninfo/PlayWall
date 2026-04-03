package de.tobias.playwall.client.net;

import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectFile;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.settings.Settings;
import de.tobias.playwall.client.view.style.color.ModernColor;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FluentClient
{
	ReadOnlyObjectProperty<ConnectionState> connectionStateProperty();

	void connect();

	void connectWithRetries(int numberOfRetries, Client.ConnectingListener listener);

	void disconnect();

	ProjectsBuilder projects();

	interface ProjectsBuilder
	{
		AllProjectsInfo list() throws PlayWallApiException;

		ProjectMetadata add(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException;

		UUID importProject(ProjectFile projectFile) throws PlayWallApiException;
	}

	ProjectBuilder project(UUID projectId);

	interface ProjectBuilder
	{
		void delete() throws PlayWallApiException;

		Project get() throws PlayWallApiException;

		void load() throws PlayWallApiException;

		ProjectFile export() throws PlayWallApiException;
	}

	ProjectCurrentBuilder currentProject();

	interface ProjectCurrentBuilder
	{
		void close() throws PlayWallApiException;

		boolean isSaved() throws PlayWallApiException;

		void save() throws PlayWallApiException;

		void undo() throws PlayWallApiException;

		void redo() throws PlayWallApiException;

		void addPage() throws PlayWallApiException;

		void reorderPages(Map<UUID, Integer> positions) throws PlayWallApiException;

		PageBuilder page(UUID pageId);

		void updateSettings(ProjectMetadata projectMetadata) throws PlayWallApiException;

		void changeGlobalVolume(double volume) throws PlayWallApiException;

		void stopAllPads() throws PlayWallApiException;

		void batchColorPads(Set<UUID> padIds, ModernColor color) throws PlayWallApiException;
	}

	interface PageBuilder
	{
		void rename(String name) throws PlayWallApiException;

		void delete() throws PlayWallApiException;

		void duplicate() throws PlayWallApiException;
	}

	interface PadBuilder
	{
		void play() throws PlayWallApiException;

		void pause() throws PlayWallApiException;

		void stop() throws PlayWallApiException;

		void newMedia(Path file) throws PlayWallApiException;

		void updateSettings(Pad pad) throws PlayWallApiException;

		void delete() throws PlayWallApiException;

		void changeVolume(double volume) throws PlayWallApiException;
	}

	PadBuilder pad(UUID padId);

	SettingsBuilder settings();

	interface SettingsBuilder
	{
		Settings get() throws PlayWallApiException;

		void update(Settings settings) throws PlayWallApiException;
	}
}
