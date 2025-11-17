package de.tobias.playwall.client.net;

import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.Page;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.model.project.ProjectMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
@Service(superclass = FluentClient.class)
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class FluentClientImpl implements FluentClient
{
	private final Client delegate;

	@Override
	public void connect()
	{
		delegate.connect();
	}

	@Override
	public void connectWithRetries(int numberOfRetries)
	{
		delegate.connectWithRetries(numberOfRetries);
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

		@Override
		public PagesBuilder pages()
		{
			return new PagesBuilderImpl(projectId);
		}

		@Override
		public PageBuilder page(UUID pageId)
		{
			return new PageBuilderImpl(projectId, pageId);
		}
	}

	@AllArgsConstructor
	private class PagesBuilderImpl implements PagesBuilder
	{

		private final UUID projectId;

		@Override
		public Page addPage(String name) throws PlayWallApiException
		{
			return delegate.addPage(projectId, name);
		}
	}

	@AllArgsConstructor
	private class PageBuilderImpl implements PageBuilder
	{
		private final UUID projectId;
		private final UUID pageId;

		@Override
		public Page rename(String name) throws PlayWallApiException
		{
			return delegate.renamePage(projectId, pageId, name);
		}

		@Override
		public void delete(String name) throws PlayWallApiException
		{
			delegate.deletePage(projectId, pageId);
		}

		@Override
		public Page duplicate(String name) throws PlayWallApiException
		{
			return delegate.duplicatePage(projectId, pageId, name);
		}
	}

	@Override
	public void play(UUID padId) throws PlayWallApiException
	{
		delegate.play(padId);
	}
}
