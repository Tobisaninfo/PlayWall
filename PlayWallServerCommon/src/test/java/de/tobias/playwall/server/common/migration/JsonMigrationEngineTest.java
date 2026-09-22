package de.tobias.playwall.server.common.migration;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonMigrationEngineTest
{
	private static MigrationRegistry registry() throws Exception
	{
		return MigrationRegistry.builder(4)
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

	@Test
	void testMigrateAppliesAllVersionsSequentially() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");

		final JsonNode migrated = engine.migrate(json("""
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
	void testMigrateStartsFromIntermediateVersion() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");

		final JsonNode migrated = engine.migrate(json("""
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
	void testMigrateDoesNotModifyOriginalDocument() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");
		final JsonNode original = json("""
				{"VERSION": 1, "layout": {"style": "grid"}, "pads": [{"id": "a", "label": "A"}]}
				""");

		final JsonNode migrated = engine.migrate(original);

		assertThat(migrated.path("VERSION").asInt()).isEqualTo(4);
		assertThat(original.path("VERSION").asInt()).isEqualTo(1);
	}

	@Test
	void testMigrateRejectsMissingVersion() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("""
				{
					"layout": {"style": "grid"},
					"legacy": {"debug": true},
					"pads": [{"id": "a", "label": "A"}]
				}
				""")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("Cannot determine version");
	}

	@Test
	void testMigrateRejectsNewerVersion() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("""
				{"VERSION": 5}""")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("newer than the supported version: 4");
	}

	@Test
	void testMigrateRejectsTooOldVersion()
	{
		final MigrationRegistry registry = MigrationRegistry.builder(4).migrateTo(4).build();
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry, "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("""
				{"VERSION": 1}""")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("too old to migrate");
	}

	@Test
	void testMigrateRejectsNonObjectDocument() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(registry(), "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("5")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("valid JSON node");
	}

	@Test
	void testAddCreatesMissingIntermediateObjects() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepAdd.of("/brand/new/field", "hello"))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "brand": {"new": {"field": "hello"}}}"""));
	}

	@Test
	void testForEachAddsDefaultValueToAllElements() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", false)))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1, "pads": [{"id": "a"}, {"id": "b"}]}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "pads": [{"id": "a", "active": false}, {"id": "b", "active": false}]}"""));
	}

	@Test
	void testForEachDeletesPropertyFromAllElements() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", new JsonMigrationStepDelete("/debug")))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1, "pads": [{"id": "a", "debug": true}, {"id": "b", "debug": false}]}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "pads": [{"id": "a"}, {"id": "b"}]}"""));
	}

	@Test
	void testForEachSupportsNestedArrays() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepForEach.of("/rows", JsonMigrationStepForEach.of("/cells", JsonMigrationStepAdd.of("/active", true))))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
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
	void testForEachRejectsMissingArray() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", true)))
				.build(), "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("""
				{"VERSION": 1, "other": {}}""")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("not an array");
	}

	@Test
	void testForEachRejectsNonObjectElement() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", true)))
				.build(), "/VERSION");

		assertThatThrownBy(() -> engine.migrate(json("""
				{"VERSION": 1, "pads": [1, 2]}""")))
				.isInstanceOf(MigrationException.class)
				.hasMessageContaining("not an object");
	}

	@Test
	void testDecodesEscapedTokens() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepAdd.of("/meta~1name", "v"))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "meta/name": "v"}"""));
	}

	@Test
	void testMoveIntoNestedPathRebuildsSource() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, new JsonMigrationStepMove("/a", "/a/b"))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1, "a": {}}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "a": {"b": {}}}"""));
	}

	@Test
	void testMoveOfNestedObjects() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, new JsonMigrationStepMove("/server/host", "/endpoint/address/host"))
				.build(), "/VERSION");

		assertThat(engine.migrate(json("""
				{"VERSION": 1, "server": {"host": "localhost", "port": 8080}}""")))
				.isEqualTo(json("""
						{"VERSION": 2, "server": {"port": 8080}, "endpoint": {"address": {"host": "localhost"}}}"""));
	}

	@Test
	void testVersionPathInsideNestedObject() throws Exception
	{
		final JsonMigrationEngine engine = new JsonMigrationEngine(MigrationRegistry.builder(2)
				.migrateTo(2, JsonMigrationStepAdd.of("/ui/fullscreen", false))
				.build(), "/metadata/VERSION");

		final JsonNode migrated = engine.migrate(json("""
				{
					"metadata": {"VERSION": 1, "name": "Project"},
					"pads": []
				}
				"""));

		assertThat(migrated.at("/metadata/VERSION").asInt()).isEqualTo(2);
		assertThat(migrated.has("/metadata/VERSION")).isFalse();
		assertThat(migrated).isEqualTo(json("""
				{
					"metadata": {"VERSION": 2, "name": "Project"},
					"pads": [],
					"ui": {"fullscreen": false}
				}
				"""));
	}

	private static JsonNode json(String source) throws Exception
	{
		return new JsonMapper().readTree(source);
	}
}