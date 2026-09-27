package de.tobias.playwall.server.config;

import de.tobias.playwall.server.common.migration.JsonMigrationStepAdd;
import de.tobias.playwall.server.common.migration.JsonMigrationStepAddToTypedObject;
import de.tobias.playwall.server.common.migration.JsonMigrationStepForEach;
import de.tobias.playwall.server.common.migration.MigrationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MigrationConfiguration
{
	@Bean
	public MigrationRegistry projectMigrationRegistry()
	{
		final JsonMigrationStepAddToTypedObject addIgnoreSoloModeStep = JsonMigrationStepAddToTypedObject.of("/content", "/@name", "AudioPadContent", "/ignoreSoloMode", false);

		return MigrationRegistry.builder(2, "/metadata/VERSION")
				.migrateTo(2,
						JsonMigrationStepAdd.of("/metadata/isSoloMode", false),
						JsonMigrationStepForEach.of("/pages",
								JsonMigrationStepForEach.of("/pads", addIgnoreSoloModeStep)))
				.build();
	}

	@Bean
	public MigrationRegistry allProjectsInfoMigrationRegistry()
	{
		return MigrationRegistry.builder(1, "/VERSION").build();
	}

	@Bean
	public MigrationRegistry settingsMigrationRegistry()
	{
		return MigrationRegistry.builder(2, "/VERSION").migrateTo(2).build();
	}
}