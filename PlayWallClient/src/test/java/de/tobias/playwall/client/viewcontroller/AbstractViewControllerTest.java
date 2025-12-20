package de.tobias.playwall.client.viewcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.extensions.AppEnvironmentSetup;
import de.tobias.playwall.client.extensions.LoggerSetup;
import de.tobias.playwall.client.mapper.ProjectMapper;
import de.tobias.playwall.client.model.project.Project;
import de.tobias.playwall.client.utils.ScreenshotOnFailure;
import de.tobias.playwall.common.api.project.model.ProjectDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@ExtendWith(LoggerSetup.class)
@ExtendWith(AppEnvironmentSetup.class)
@ExtendWith(ApplicationExtension.class)
@ExtendWith(ScreenshotOnFailure.class)
public abstract class AbstractViewControllerTest
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
