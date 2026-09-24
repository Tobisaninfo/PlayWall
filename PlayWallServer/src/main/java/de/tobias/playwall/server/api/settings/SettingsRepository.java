package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.common.migration.JsonMigrationResult;
import de.tobias.playwall.server.common.migration.MigrationRegistry;
import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.common.storage.PathProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class SettingsRepository
{
	private static final String SETTINGS_FILENAME = "settings.json";

	private final PathProvider pathProvider;
	private final JsonMapper mapper;
	private final MigrationRegistry settingsMigrationRegistry;

	public SettingsRepository(PathProvider pathProvider, JsonMapper mapper,
							  @Qualifier("settingsMigrationRegistry") MigrationRegistry settingsMigrationRegistry)
	{
		this.pathProvider = pathProvider;
		this.mapper = mapper;
		this.settingsMigrationRegistry = settingsMigrationRegistry;
	}

	public Settings loadSettings() throws IOException
	{
		final Path path = pathProvider.getPathForConfig(SETTINGS_FILENAME);
		if(Files.notExists(path))
		{
			log.debug("No settings.json found, creating default settings file in: \"{}\"", path);
			saveSettings(Settings.DEFAULT);
		}

		final JsonNode root = mapper.readTree(Files.newBufferedReader(path));
		final JsonMigrationResult migrationResult = settingsMigrationRegistry.migrate(root);
		final Settings settings = mapper.treeToValue(migrationResult.node(), Settings.class);

		if(migrationResult.isMigrated())
		{
			saveSettings(settings);
		}

		return settings;
	}

	public void saveSettings(Settings settings) throws IOException
	{
		final Path path = pathProvider.getPathForConfig(SETTINGS_FILENAME);
		Files.createDirectories(path.getParent());
		mapper.writeValue(Files.newBufferedWriter(path), settings);
	}
}