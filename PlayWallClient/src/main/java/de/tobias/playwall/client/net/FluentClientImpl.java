package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;
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
		public List<ProjectMetadata> list() throws PlayWallApiException
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
		public Project launch() throws PlayWallApiException
		{
			return delegate.launchProject(projectId);
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
		public void save() throws PlayWallApiException
		{
			delegate.saveProject();
		}

		@Override
		public Page addPage(String name) throws PlayWallApiException
		{
			return delegate.addPage(name);
		}
	}

	@AllArgsConstructor
	private class PageBuilderImpl implements PageBuilder
	{
		private final UUID pageId;

		@Override
		public Page rename(String name) throws PlayWallApiException
		{
			return delegate.renamePage(pageId, name);
		}

		@Override
		public void delete(String name) throws PlayWallApiException
		{
			delegate.deletePage(pageId);
		}

		@Override
		public Page duplicate(String name) throws PlayWallApiException
		{
			return delegate.duplicatePage(pageId, name);
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
	}

	@Override
	public PadBuilder pad(UUID padId)
	{
		return new PadBuilderImpl(padId);
	}
}
