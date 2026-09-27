package de.tobias.playwall.server.common.migration;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;

import java.util.*;

/**
 * Registry of migration steps keyed by the format version they produce. A registration for version
 * {@code v} describes what has to change to turn a JSON node of version {@code v - 1} into version {@code v}.
 *
 * <p>Supported versions must be contiguous: every version between the first registered migration and
 * {@link #currentVersion()} has to be registered (empty step lists are allowed) so that any supported
 * JSON node can be migrated to the latest version by applying the migrations in order.
 *
 * <p>The registry also performs the migrations: {@link #migrate(JsonNode)} migrates a JSON node to the latest
 * version defined by {@link #currentVersion()}.
 */
@Slf4j
public final class MigrationRegistry
{
	private final int currentVersion;
	private final String versionPath;
	private final SortedMap<Integer, List<JsonMigrationStep>> steps;

	private MigrationRegistry(int currentVersion, String versionPath, SortedMap<Integer, List<JsonMigrationStep>> steps)
	{
		this.currentVersion = currentVersion;
		this.versionPath = versionPath;
		this.steps = Collections.unmodifiableSortedMap(steps);
	}

	public static Builder builder(int currentVersion, String versionPath)
	{
		if(currentVersion < 1)
		{
			throw new IllegalArgumentException("currentVersion must be at least 1: " + currentVersion);
		}
		return new Builder(currentVersion, versionPath);
	}

	public int currentVersion()
	{
		return currentVersion;
	}

	public String versionPath()
	{
		return versionPath;
	}

	public int getMinSupportedVersion()
	{
		return steps.isEmpty() ? currentVersion : steps.firstKey() - 1;
	}

	public List<JsonMigrationStep> getStepsByTargetVersion(int targetVersion)
	{
		final List<JsonMigrationStep> versionSteps = steps.get(targetVersion);
		return versionSteps == null ? List.of() : versionSteps;
	}

	/**
	 * Migrates the given JSON node to {@link #currentVersion()}.
	 *
	 * @param node the JSON node to migrate
	 * @return a migrated deep copy of the node
	 * @throws MigrationException if the node is not an object, is too old to migrate, or newer than the current version
	 */
	public JsonMigrationResult migrate(JsonNode node)
	{
		if(node == null)
		{
			throw new MigrationException("Node must not be null");
		}

		if(!node.isObject())
		{
			throw new MigrationException("Node must be a valid JSON node");
		}

		final JsonNode root = node.deepCopy();
		final int version = parseVersion(root);

		if(version > currentVersion)
		{
			throw new MigrationException("Format version " + version + " is newer than the supported version: " + currentVersion);
		}

		if(version < getMinSupportedVersion())
		{
			throw new MigrationException("Format version " + version + " is too old to migrate. Minimum supported version is: " + getMinSupportedVersion());
		}

		int current = version;
		boolean isMigrated = false;
		while(current < currentVersion)
		{
			log.debug("Migrate JSON from version {} to {}", current, current + 1);
			for(final JsonMigrationStep step : getStepsByTargetVersion(current + 1))
			{
				apply(root, step);
			}

			current++;
			JsonPathOperations.add(root, versionPath, new IntNode(current));
			isMigrated = true;
		}

		return new JsonMigrationResult(root, isMigrated);
	}

	public int parseVersion(JsonNode root)
	{
		final JsonNode versionNode = root.at(versionPath);
		if(versionNode.isMissingNode())
		{
			throw new MigrationException("Cannot determine version");
		}

		return versionNode.asInt();
	}

	private static void apply(JsonNode root, JsonMigrationStep step)
	{
		switch(step)
		{
			case JsonMigrationStepAdd(String path, JsonNode value) -> JsonPathOperations.add(root, path, value);
			case JsonMigrationStepDelete(String path) -> JsonPathOperations.remove(root, path);
			case JsonMigrationStepMove(String from, String to) ->
			{
				final JsonNode value = JsonPathOperations.remove(root, from);
				JsonPathOperations.add(root, to, value);
			}
			case JsonMigrationStepForEach forEachStep -> applyToEach(root, forEachStep);
			case JsonMigrationStepAddToTypedObject typedObjectStep -> addToTypedObject(root, typedObjectStep);
		}
	}

	private static void addToTypedObject(JsonNode root, JsonMigrationStepAddToTypedObject step)
	{
		final JsonNode object = root.at(JsonPointer.compile(step.objectPath()));
		if(!object.isObject())
		{
			return;
		}

		final JsonNode type = object.at(JsonPointer.compile(step.typeProperty()));
		if(!step.typeValue().equals(type.asString("")))
		{
			return;
		}

		JsonPathOperations.add(object, step.path(), step.value());
	}

	private static void applyToEach(JsonNode root, JsonMigrationStepForEach step)
	{
		final JsonNode array = root.at(JsonPointer.compile(step.arrayPath()));
		if(array.isMissingNode() || !array.isArray())
		{
			throw new MigrationException("Cannot apply for-each at '" + step.arrayPath() + "': target is not an array");
		}

		for(final JsonNode element : array)
		{
			if(!element.isObject())
			{
				throw new MigrationException("Cannot apply for-each at '" + step.arrayPath() + "': array element is not an object");
			}

			apply(element, step.step());
		}
	}

	public static final class Builder
	{
		private final int currentVersion;
		private final String versionPath;
		private final SortedMap<Integer, List<JsonMigrationStep>> steps = new TreeMap<>();

		private Builder(int currentVersion, String versionPath)
		{
			this.currentVersion = currentVersion;
			this.versionPath = versionPath;
		}

		public Builder migrateTo(int targetVersion, JsonMigrationStep... migrationSteps)
		{
			if(targetVersion < 2 || targetVersion > currentVersion)
			{
				throw new IllegalArgumentException("Migration version must satisfy 2 <= targetVersion <= currentVersion (" + currentVersion + "): " + targetVersion);
			}

			if(steps.containsKey(targetVersion))
			{
				throw new IllegalArgumentException("Duplicate migration registered for format version " + targetVersion);
			}

			final List<JsonMigrationStep> list = new ArrayList<>(migrationSteps.length);
			for(final JsonMigrationStep step : migrationSteps)
			{
				list.add(Objects.requireNonNull(step, "step"));
			}

			steps.put(targetVersion, List.copyOf(list));
			return this;
		}

		public MigrationRegistry build()
		{
			JsonMigrationStep.validatePath(versionPath);

			if(steps.isEmpty())
			{
				return new MigrationRegistry(currentVersion, versionPath, new TreeMap<>());
			}

			final int first = steps.firstKey();
			for(int target = first; target <= currentVersion; target++)
			{
				if(!steps.containsKey(target))
				{
					throw new IllegalArgumentException("Missing migration for format version " + target);
				}
			}

			return new MigrationRegistry(currentVersion, versionPath, new TreeMap<>(steps));
		}
	}
}