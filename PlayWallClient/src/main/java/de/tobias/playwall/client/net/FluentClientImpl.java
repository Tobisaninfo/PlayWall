package de.tobias.playwall.client.net;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.project.AllProjectsInfo;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.domain.settings.Settings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
@Service(superclass = FluentClient.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
class FluentClientImpl implements FluentClient
{
	private final Client delegate;

	@Override
	public void connect()
	{
		delegate.connect();
	}

	@Override
	public void connectWithRetries(int numberOfRetries, Client.ConnectingListener listener)
	{
		delegate.connectWithRetries(numberOfRetries, listener);
	}

	@Override
	public void disconnect()
	{
		delegate.disconnect();
	}

	@Override
	public ProjectsBuilder projects()
	{
		return new ProjectsBuilderImpl();
	}

	@Override
	public ProjectBuilder project(UUID projectId)
	{
		return new ProjectBuilderImpl(projectId);
	}

	@Override
	public ProjectCurrentBuilder currentProject()
	{
		return new ProjectCurrentBuilderImpl();
	}

	private class ProjectsBuilderImpl implements ProjectsBuilder
	{

		@Override
		public AllProjectsInfo list() throws PlayWallApiException
		{
			return delegate.getProjects();
		}

		@Override
		public ProjectMetadata add(String name, int numberOfHorizontalPads, int numberOfVerticalPads) throws PlayWallApiException
		{
			return delegate.addProject(name, numberOfHorizontalPads, numberOfVerticalPads);
		}
	}

	@AllArgsConstructor
	private class ProjectBuilderImpl implements ProjectBuilder
	{
		private final UUID projectId;

		@Override
		public Project get() throws PlayWallApiException
		{
			return delegate.getProject(projectId);
		}

		@Override
		public void load() throws PlayWallApiException
		{
			delegate.loadProject(projectId);
		}

		@Override
		public void delete() throws PlayWallApiException
		{
			delegate.deleteProject(projectId);
		}

	}

	private class ProjectCurrentBuilderImpl implements ProjectCurrentBuilder
	{
		@Override
		public PageBuilder page(UUID pageId)
		{
			return new PageBuilderImpl(pageId);
		}

		@Override
		public void close() throws PlayWallApiException
		{
			delegate.closeProject();
		}

		@Override
		public boolean isSaved() throws PlayWallApiException
		{
			return delegate.isSaved();
		}

		@Override
		public void save() throws PlayWallApiException
		{
			delegate.saveProject();
		}

		@Override
		public void undo() throws PlayWallApiException
		{
			delegate.undo();
		}

		@Override
		public void redo() throws PlayWallApiException
		{
			delegate.redo();
		}

		@Override
		public void addPage() throws PlayWallApiException
		{
			delegate.addPage();
		}

		@Override
		public void reorderPages(Map<UUID, Integer> positions) throws PlayWallApiException
		{
			delegate.reorderPage(positions);
		}

		@Override
		public void updateSettings(ProjectMetadata projectMetadata) throws PlayWallApiException
		{
			delegate.updateProjectSettings(projectMetadata);
		}

		@Override
		public void changeGlobalVolume(double volume) throws PlayWallApiException
		{
			delegate.changeGlobalVolume(volume);
		}

		@Override
		public void stopAllPads() throws PlayWallApiException
		{
			delegate.stopAllPads();
		}
	}

	@AllArgsConstructor
	private class PageBuilderImpl implements PageBuilder
	{
		private final UUID pageId;

		@Override
		public void rename(String name) throws PlayWallApiException
		{
			delegate.renamePage(pageId, name);
		}

		@Override
		public void delete() throws PlayWallApiException
		{
			delegate.deletePage(pageId);
		}

		@Override
		public void duplicate() throws PlayWallApiException
		{
			delegate.duplicatePage(pageId);
		}
	}

	@AllArgsConstructor
	private class PadBuilderImpl implements PadBuilder
	{
		private final UUID padId;

		@Override
		public void play() throws PlayWallApiException
		{
			delegate.play(padId);
		}

		@Override
		public void pause() throws PlayWallApiException
		{
			delegate.pause(padId);
		}

		@Override
		public void stop() throws PlayWallApiException
		{
			delegate.stop(padId);
		}

		@Override
		public void newMedia(Path file) throws PlayWallApiException
		{
			delegate.newMedia(padId, file);
		}

		@Override
		public void updateSettings(Pad pad) throws PlayWallApiException
		{
			delegate.updateSettings(padId, pad);
		}

		@Override
		public void delete() throws PlayWallApiException
		{
			delegate.deletePad(padId);
		}

		@Override
		public void changeVolume(double volume) throws PlayWallApiException
		{
			delegate.changeVolume(padId, volume);
		}
	}

	@Override
	public PadBuilder pad(UUID padId)
	{
		return new PadBuilderImpl(padId);
	}

	private class SettingsBuilderImpl implements SettingsBuilder
	{
		@Override
		public Settings get() throws PlayWallApiException
		{
			return delegate.getProgramSettings();
		}

		@Override
		public void update(Settings settings) throws PlayWallApiException
		{
			delegate.updateProgramSettings(settings);
		}
	}

	@Override
	public SettingsBuilder settings()
	{
		return new SettingsBuilderImpl();
	}


}
