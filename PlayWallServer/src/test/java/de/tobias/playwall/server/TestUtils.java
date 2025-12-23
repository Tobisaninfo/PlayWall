package de.tobias.playwall.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tobias.playwall.server.common.model.project.Project;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class TestUtils
{
	public static Project loadProject(ObjectMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		try
		{
			return mapper.readValue(resourceAsStream, Project.class);
		}
		catch(IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}
