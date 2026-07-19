package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.ProjectSaveRequest;
import de.tobias.playwall.server.api.project.AllProjectsInfoRepository;
import de.tobias.playwall.server.api.project.ProjectRepository;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import de.tobias.playwall.server.project.ProjectSaveLock;
import lombok.AllArgsConstructor;

import java.io.IOException;

@RequestHandlerTyped(ProjectSaveRequest.class)
@AllArgsConstructor
class ProjectSaveHandler implements OneTimeActionRequestHandler<ProjectSaveRequest>
{
	private final ProjectController projectController;
	private final ProjectRepository projectRepository;
	private final AllProjectsInfoRepository allProjectsInfoRepository;
	private final ProjectSaveLock projectSaveLock;

	@Override
	public void handleRequest(ProjectSaveRequest requestMessage) throws IOException
	{
		projectSaveLock.executeSave(() -> {
			try
			{
				projectRepository.saveProject(projectController.getLoadedProject());
				allProjectsInfoRepository.saveAllProjectsInfo();
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
}
