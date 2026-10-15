package de.tobias.playwall.server.config;

import de.tobias.playwall.server.common.migration.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MigrationConfiguration
{
	@Bean
	public MigrationRegistry projectMigrationRegistry()
	{
		return MigrationRegistry.builder(3, "/metadata/VERSION")
				.migrateTo(2,
						JsonMigrationStepAdd.of("/metadata/isSoloMode", false),
						JsonMigrationStepForEach.of("/pages",
								JsonMigrationStepForEach.of("/pads",
										JsonMigrationStepAddToTypedObject.of("/content", "/@name", "AudioPadContent", "/ignoreSoloMode", false))))
				.migrateTo(3,
						JsonMigrationStepForEachValue.of("/metadata/mappings",
								JsonMigrationStepAddToTypedObject.of("/action", "/type", "stopAll", "/shouldFadeOut", true)))
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