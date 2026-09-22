package de.tobias.playwall.server.config;

import de.tobias.playwall.server.common.migration.MigrationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MigrationConfiguration
{
	@Bean
	public MigrationRegistry projectMigrationRegistry()
	{
		return MigrationRegistry.builder(1, "/metadata/VERSION").build();
	}

	@Bean
	public MigrationRegistry allProjectsInfoMigrationRegistry()
	{
		return MigrationRegistry.builder(1, "/VERSION").build();
	}
}