package de.tobias.playwall.server.common.migration;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationRegistryTest
{
	@Test
	void testStepsAreRegisteredPerVersion()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(4, "/VERSION")
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
		final MigrationRegistry registry = MigrationRegistry.builder(1, "/VERSION").build();

		assertThat(registry.currentVersion()).isEqualTo(1);
		assertThat(registry.getMinSupportedVersion()).isEqualTo(1);
		assertThat(registry.getStepsByTargetVersion(1)).isEmpty();
	}

	@Test
	void testVersionPathIsExposed()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(1, "/VERSION").build();

		assertThat(registry.versionPath()).isEqualTo("/VERSION");
	}

	@Test
	void testBuildRejectsInvalidVersionPath()
	{
		final MigrationRegistry.Builder builder = MigrationRegistry.builder(1, "VERSION");

		assertThatThrownBy(builder::build)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("starting with '/'");
	}

	@Test
	void testRejectsGapBetweenVersions()
	{
		final MigrationRegistry.Builder builder = MigrationRegistry.builder(4, "/VERSION")
				.migrateTo(2)
				.migrateTo(4);

		assertThatThrownBy(builder::build)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Missing migration for format version 3");
	}

	@Test
	void testRejectsDuplicateVersion()
	{
		final MigrationRegistry.Builder builder = MigrationRegistry.builder(3, "/VERSION").migrateTo(2);

		assertThatThrownBy(() -> builder.migrateTo(2))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Duplicate migration registered for format version 2");
	}

	@Test
	void testRejectsOutOfRangeVersion()
	{
		final MigrationRegistry.Builder builder = MigrationRegistry.builder(3, "/VERSION");

		assertThatThrownBy(() -> builder.migrateTo(4))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("2 <= targetVersion <= currentVersion");
		assertThatThrownBy(() -> builder.migrateTo(1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("2 <= targetVersion <= currentVersion");
	}

	@Test
	void testRejectsSelfMove()
	{
		assertThatThrownBy(() -> new JsonMigrationStepMove("/a", "/a"))
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

	@Test
	void testMigrateAppliesAllVersionsSequentially()
	{
		final JsonNode migrated = registry().migrate(json("""
				{
					"VERSION": 1,
					"layout": {"style": "grid"},
					"legacy": {"debug": true},
					"pads": [{"id": "a", "label": "A"}]
				}
				"""));

		assertThat(migrated).isEqualTo(json("""
				{
					"VERSION": 4,
					"ui": {"layout": {"style": "grid"}, "fullscreen": false, "autosave": true},
					"pads": [{"id": "a", "name": "A", "active": true}]
				}
				"""));
	}

	@Test
	void testMigrateStartsFromIntermediateVersion()
	{
		final JsonNode migrated = registry().migrate(json("""
				{
					"VERSION": 3,
					"ui": {"layout": {"style": "grid"}, "fullscreen": false, "autosave": true},
					"pads": [{"id": "a", "name": "A"}]
				}
				"""));

		assertThat(migrated).isEqualTo(json("""
				{
					"VERSION": 4,
					"ui": {"layout": {"style": "grid"}, "fullscreen": false, "autosave": true},
					"pads": [{"id": "a", "name": "A", "active": true}]
				}
				"""));
	}

	@Test
	void testMigrateDoesNotModifyOriginalDocument()
	{
		final JsonNode original = json("""
				{"VERSION": 1, "layout": {"style": "grid"}, "pads": [{"id": "a", "label": "A"}]}
				""");

		final JsonNode migrated = registry().migrate(original);

		assertThat(migrated.path("VERSION").asInt()).isEqualTo(4);
		assertThat(original.path("VERSION").asInt()).isEqualTo(1);
	}

	@Test
	void testDeleteOfMissingPathIsIgnored()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, new JsonMigrationStepDelete("/missing"))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1}""")))
				.isEqualTo(json("""
						{"VERSION": 2}"""));
	}

	@Test
	void testMigrateRejectsMissingVersion()
	{
		final MigrationRegistry registry = registry();
		final JsonNode node = json("""
				{
					"layout": {"style": "grid"},
					"legacy": {"debug": true},
					"pads": [{"id": "a", "label": "A"}]
				}
				""");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("Cannot determine version");
	}

	@Test
	void testMigrateRejectsNewerVersion()
	{
		final MigrationRegistry registry = registry();
		final JsonNode node = json("""
				{"VERSION": 5}""");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("newer than the supported version: 4");
	}

	@Test
	void testMigrateRejectsTooOldVersion()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(4, "/VERSION").migrateTo(4).build();
		final JsonNode node = json("""
				{"VERSION": 1}""");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("too old to migrate");
	}

	@Test
	void testMigrateRejectsNonObjectDocument()
	{
		final MigrationRegistry registry = registry();
		final JsonNode node = json("5");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("valid JSON node");
	}

	@Test
	void testAddCreatesMissingIntermediateObjects()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepAdd.of("/brand/new/field", "hello"))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "brand": {"new": {"field": "hello"}}}"""));
	}

	@Test
	void testForEachAddsDefaultValueToAllElements()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", false)))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1, "pads": [{"id": "a"}, {"id": "b"}]}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "pads": [{"id": "a", "active": false}, {"id": "b", "active": false}]}"""));
	}

	@Test
	void testForEachDeletesPropertyFromAllElements()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", new JsonMigrationStepDelete("/debug")))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1, "pads": [{"id": "a", "debug": true}, {"id": "b", "debug": false}]}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "pads": [{"id": "a"}, {"id": "b"}]}"""));
	}

	@Test
	void testForEachSupportsNestedArrays()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepForEach.of("/rows", JsonMigrationStepForEach.of("/cells", JsonMigrationStepAdd.of("/active", true))))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1, "rows": [{"cells": [{"id": "c1"}, {"id": "c2"}]}, {"cells": [{"id": "c3"}]}]}
				""")))
				.isEqualTo(json("""
						{"VERSION": 2, "rows": [
							{"cells": [{"id": "c1", "active": true}, {"id": "c2", "active": true}]},
							{"cells": [{"id": "c3", "active": true}]}
						]}
						"""));
	}

	@Test
	void testForEachRejectsMissingArray()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", true)))
				.build();
		final JsonNode node = json("""
				{"VERSION": 1, "other": {}}""");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("not an array");
	}

	@Test
	void testForEachRejectsNonObjectElement()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", true)))
				.build();
		final JsonNode node = json("""
				{"VERSION": 1, "pads": [1, 2]}""");

		assertThatThrownBy(() -> registry.migrate(node))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("not an object");
	}

	@Test
	void testDecodesEscapedTokens()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, JsonMigrationStepAdd.of("/meta~1name", "v"))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "meta/name": "v"}"""));
	}

	@Test
	void testMoveIntoNestedPathRebuildsSource()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, new JsonMigrationStepMove("/a", "/a/b"))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1, "a": {}}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "a": {"b": {}}}"""));
	}

	@Test
	void testMoveOfNestedObjects()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/VERSION")
				.migrateTo(2, new JsonMigrationStepMove("/server/host", "/endpoint/address/host"))
				.build();

		assertThat(registry.migrate(json("""
				{"VERSION": 1, "server": {"host": "localhost", "port": 8080}}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "server": {"port": 8080}, "endpoint": {"address": {"host": "localhost"}}}"""));
	}

	@Test
	void testVersionPathInsideNestedObject()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(2, "/metadata/VERSION")
				.migrateTo(2, JsonMigrationStepAdd.of("/ui/fullscreen", false))
				.build();

		final JsonNode migrated = registry.migrate(json("""
				{
					"metadata": {"VERSION": 1, "name": "Project"},
					"pads": []
				}
				"""));

		assertThat(migrated.at("/metadata/VERSION").asInt()).isEqualTo(2);
		assertThat(migrated).isEqualTo(json("""
				{
					"metadata": {"VERSION": 2, "name": "Project"},
					"pads": [],
					"ui": {"fullscreen": false}
				}
				"""));
	}

	private static MigrationRegistry registry()
	{
		return MigrationRegistry.builder(4, "/VERSION")
				.migrateTo(2,
						new JsonMigrationStepMove("/layout", "/ui/layout"),
						JsonMigrationStepAdd.of("/ui/fullscreen", false))
				.migrateTo(3,
						new JsonMigrationStepDelete("/legacy"),
						new JsonMigrationStepMove("/pads/0/label", "/pads/0/name"),
						JsonMigrationStepAdd.of("/ui/autosave", true))
				.migrateTo(4,
						JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", true)))
				.build();
	}

	private static JsonNode json(String source)
	{
		return new JsonMapper().readTree(source);
	}
}