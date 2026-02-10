package de.tobias.playwall.utils;

import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMapper;
import de.tobias.playwall.client.extensions.AppEnvironmentSetup;
import de.tobias.playwall.client.extensions.LoggerSetup;
import de.tobias.playwall.common.api.project.model.ProjectDto;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

@ExtendWith(LoggerSetup.class)
@ExtendWith(AppEnvironmentSetup.class)
public abstract class AbstractTest
{
	protected Project loadProject(String resourcePath)
	{
		final ProjectMapper projectMapper = AppContextHolder.getInstance().get(ProjectMapper.class);

		final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
		final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
		return projectMapper.projectDtoToProject(mapper.readValue(resourceAsStream, ProjectDto.class));
	}
}
