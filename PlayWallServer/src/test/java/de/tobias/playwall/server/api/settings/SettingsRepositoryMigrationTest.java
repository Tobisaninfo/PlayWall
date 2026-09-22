package de.tobias.playwall.server.api.settings;

import de.tobias.playwall.server.api.AbstractRequestHandlerTest;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.migration.MigrationException;
import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.common.storage.PathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SettingsRepositoryMigrationTest extends AbstractRequestHandlerTest
{
	private static final String V2_SETTINGS = """
			{
				"VERSION": 2,
				"autoLoadLatestProjectOnStart": false,
				"autosave": false,
				"unsavedChangesMode": "ASK",
				"selectedAudioDevice": null
			}
			""";

	private static final String V1_SETTINGS = """
			{
				"VERSION": 1,
				"autoLoadLatestProjectOnStart": false,
				"autosave": false,
				"unsavedChangesMode": "ASK",
				"selectedAudioDevice": null
			}
			""";

	@TempDir
	private Path tempDir;

	@MockitoBean
	private PathProvider pathProvider;

	@MockitoBean
	private AudioHandlerFactory audioHandlerFactory;

	@Autowired
	private SettingsRepository settingsRepository;

	@BeforeEach
	void beforeEach()
	{
		when(pathProvider.getPathForConfig(any())).thenReturn(tempDir.resolve("settings.json"));
	}

	@Test
	void testLoadSettings_currentVersionIsLoadedAndNotRewritten() throws IOException
	{
		final Path file = tempDir.resolve("settings.json");
		Files.writeString(file, V2_SETTINGS);

		final Settings settings = settingsRepository.loadSettings();

		assertThat(settings).isEqualTo(Settings.builder().build());
		assertThat(Files.readString(file)).isEqualTo(V2_SETTINGS);
	}

	@Test
	void testLoadSettings_legacyVersionIsMigratedAndRewrittenToVersion2() throws IOException
	{
		final Path file = tempDir.resolve("settings.json");
		Files.writeString(file, V1_SETTINGS);

		final Settings settings = settingsRepository.loadSettings();

		assertThat(settings).isEqualTo(Settings.builder().build());
		assertThat(Files.readString(file)).contains("\"VERSION\":2");
	}

	@Test
	void testLoadSettings_missingVersionIsRejected() throws IOException
	{
		Files.writeString(tempDir.resolve("settings.json"), """
				{
					"autoLoadLatestProjectOnStart": false,
					"autosave": false,
					"unsavedChangesMode": "ASK",
					"selectedAudioDevice": null
				}
				""");

		assertThatThrownBy(() -> settingsRepository.loadSettings())
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("Cannot determine version");
	}

	@Test
	void testLoadSettings_tooOldVersionIsRejected() throws IOException
	{
		Files.writeString(tempDir.resolve("settings.json"), """
				{
					"VERSION": 0,
					"autoLoadLatestProjectOnStart": false
				}
				""");

		assertThatThrownBy(() -> settingsRepository.loadSettings())
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("too old to migrate");
	}

	@Test
	void testLoadSettings_newerVersionIsRejected() throws IOException
	{
		Files.writeString(tempDir.resolve("settings.json"), """
				{
					"VERSION": 999,
					"autoLoadLatestProjectOnStart": false
				}
				""");

		assertThatThrownBy(() -> settingsRepository.loadSettings())
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("newer than the supported version");
	}
}