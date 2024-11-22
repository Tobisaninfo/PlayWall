package de.tobias.playwall.client.net;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.mapper.ProjectMetadataMapper;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.*;
import de.tobias.playwall.common.utils.MapUtils;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static de.tobias.playwall.common.utils.MapUtils.entry;

public class ClientImpl implements Client
{
	private final ProjectMetadataMapper projectMetadataMapper;

	private ClientWebSocketHandler clientWebSocketHandler;

	public ClientImpl(ProjectMetadataMapper projectMetadataMapper)
	{
		this.projectMetadataMapper = projectMetadataMapper;
	}

	@Override
	public void connect()
	{
		final String clientId = UUID.randomUUID().toString();
		Logger.info("Connect to server with client id {0}", clientId);

		clientWebSocketHandler = new ClientWebSocketHandler();
		clientWebSocketHandler.connect(MapUtils.create(entry("clientId", clientId)));
		Logger.info("Connected");
	}

	@Override
	public void disconnect()
	{
		clientWebSocketHandler.disconnect();
	}

	@Override
	public void getProjects(Consumer<List<Project>> callback)
	{
		clientWebSocketHandler.send(new ProjectListRequest(), (ProjectListResponse res) -> {
			callback.accept(res.getProjects().stream()
					.map(projectMetadataMapper::projectMetadataDtoToProject)
					.sorted(Comparator.comparing(Project::name))
					.toList());
		});
	}

	@Override
	public void addProject(String name, BiConsumer<Boolean, Project> callback)
	{
		clientWebSocketHandler.send(new ProjectAddRequest(name), (ProjectAddResponse res) ->
				callback.accept(res.isSuccess(), projectMetadataMapper.projectMetadataDtoToProject(res.getProject())));
	}

	@Override
	public void deleteProject(Project project, Consumer<ProjectDeleteResponse> callback)
	{
		clientWebSocketHandler.send(new ProjectDeleteRequest(project.id()), callback);
	}
}
