package de.tobias.playwall.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.extensions.AppEnvironmentSetup;
import de.tobias.playwall.client.extensions.LoggerSetup;
import de.tobias.playwall.client.mapper.ProjectMapper;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.common.api.project.model.ProjectDto;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@ExtendWith(LoggerSetup.class)
@ExtendWith(AppEnvironmentSetup.class)
public abstract class AbstractTest
{
	protected Project loadProject(String resourcePath)
	{
		final ProjectMapper projectMapper = AppContextHolder.getInstance().get(ProjectMapper.class);

		final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			return projectMapper.projectDtoToProject(mapper.readValue(resourceAsStream, ProjectDto.class));
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
