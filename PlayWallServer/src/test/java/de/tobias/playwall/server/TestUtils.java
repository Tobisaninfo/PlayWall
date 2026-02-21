package de.tobias.playwall.server;

import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.settings.Settings;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

public class TestUtils
{
	public static Project loadProject(JsonMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		return mapper.readValue(resourceAsStream, Project.class);
	}

	public static Settings loadSettings(JsonMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		return mapper.readValue(resourceAsStream, Settings.class);
	}
}
