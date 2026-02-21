package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsRepository
{
	private static final String SETTINGS_FILENAME = "settings.json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;

	public Settings loadSettings() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(SETTINGS_FILENAME);
		if(!Files.exists(path))
		{
			log.debug("No settings.json found, creating default settings file in: \"{}\"", path);
			saveSettings(Settings.DEFAULT);
		}

		return mapper.readValue(Files.newBufferedReader(path), Settings.class);
	}

	public void saveSettings(Settings settings) throws IOException
	{
		final Path path = pathProvider.getPathForConfig(SETTINGS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), settings);
	}
}
