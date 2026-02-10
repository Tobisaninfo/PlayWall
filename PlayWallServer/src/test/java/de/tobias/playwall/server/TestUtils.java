package de.tobias.playwall.server;

import de.tobias.playwall.server.common.model.project.Project;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

public class TestUtils
{
	public static Project loadProject(JsonMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		return mapper.readValue(resourceAsStream, Project.class);
	}
}
