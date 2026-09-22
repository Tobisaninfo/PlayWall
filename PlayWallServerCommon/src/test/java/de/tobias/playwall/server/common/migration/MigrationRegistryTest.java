package de.tobias.playwall.server.common.migration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationRegistryTest
{
	@Test
	void testStepsAreRegisteredPerVersion()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(4)
				.migrateTo(2, new JsonMigrationStepDelete("/a"))
				.migrateTo(3)
				.migrateTo(4)
				.build();

		assertThat(registry.currentVersion()).isEqualTo(4);
		assertThat(registry.getMinSupportedVersion()).isEqualTo(1);
		assertThat(registry.getStepsByTargetVersion(2)).containsExactly(new JsonMigrationStepDelete("/a"));
		assertThat(registry.getStepsByTargetVersion(3)).isEmpty();
	}

	@Test
	void testEmptyRegistryTreatsCurrentVersionAsFirstVersion()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(1).build();

		assertThat(registry.currentVersion()).isEqualTo(1);
		assertThat(registry.getMinSupportedVersion()).isEqualTo(1);
		assertThat(registry.getStepsByTargetVersion(1)).isEmpty();
	}

	@Test
	void testRejectsGapBetweenVersions()
	{
		assertThatThrownBy(() -> MigrationRegistry.builder(4)
				.migrateTo(2)
				.migrateTo(4)
				.build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Missing migration for format version 3");
	}

	@Test
	void testRejectsDuplicateVersion()
	{
		assertThatThrownBy(() -> MigrationRegistry.builder(3)
				.migrateTo(2)
				.migrateTo(2))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Duplicate migration registered for format version 2");
	}

	@Test
	void testRejectsOutOfRangeVersion()
	{
		assertThatThrownBy(() -> MigrationRegistry.builder(3).migrateTo(4))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("2 <= targetVersion <= currentVersion");
		assertThatThrownBy(() -> MigrationRegistry.builder(3).migrateTo(1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("2 <= targetVersion <= currentVersion");
	}

	@Test
	void testRejectsSelfMove()
	{
		assertThatThrownBy(() -> MigrationRegistry.builder(3).migrateTo(2, new JsonMigrationStepMove("/a", "/a")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Cannot move path onto itself");
	}

	@Test
	void testRejectsInvalidPath()
	{
		assertThatThrownBy(() -> new JsonMigrationStepAdd("a/b", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("starting with '/'");
	}
}