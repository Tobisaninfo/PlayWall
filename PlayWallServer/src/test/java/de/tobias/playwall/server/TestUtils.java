package de.tobias.playwall.server;

import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.settings.Settings;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.file.Path;

public class TestUtils
{
	public static Project loadProject(JsonMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		return mapper.readValue(resourceAsStream, Project.class);
	}

	public static Project loadProject(JsonMapper mapper, Path path)
	{
		return mapper.readValue(path, Project.class);
	}

	public static Settings loadSettings(JsonMapper mapper, String resourcePath)
	{
		final InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
		return mapper.readValue(resourceAsStream, Settings.class);
	}
}
