package de.tobias.playwall.server.common.storage;

import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playwall.server.common.config.properties.PathProviderProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class PathProvider
{
	private final PathProviderProperties providerProperties;

	private String getPlatformSpecificAppDataDirectory()
	{
		return SystemUtils.getApplicationSupportDirectoryPath().toString();
	}

	private Path getBaseDirectory()
	{
		return Paths.get(providerProperties.getBaseDirectoryTemplate().replace("{appdata}", getPlatformSpecificAppDataDirectory()));
	}

	public Path getPathForProject(String projectId)
	{
		return getBaseDirectory().resolve("projects").resolve(projectId);
	}

	public Path getPathForConfig(String configFileName)
	{
		return getBaseDirectory().resolve("config").resolve(configFileName);
	}
}
