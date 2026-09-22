package de.tobias.playwall.server.common.migration;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;

/**
 * Migrates JSON nodes to the latest format version by applying all migrations sequentially.
 * Nodes are never modified in place. The engine works on a deep copy and returns a new, migrated tree.
 */
public final class JsonMigrationEngine
{
	private final MigrationRegistry registry;
	private final String versionPath;

	public JsonMigrationEngine(MigrationRegistry registry, String versionPath)
	{
		this.registry = registry;
		this.versionPath = versionPath;
		JsonMigrationStep.validatePath(versionPath);
	}

	/**
	 * Migrates the given JSON node to {@link MigrationRegistry#currentVersion()}.
	 *
	 * @param node the JSON node to migrate
	 * @return a migrated deep copy of the node
	 * @throws MigrationException if the node is not an object, is too old to migrate, or newer than the current version
	 */
	public JsonNode migrate(JsonNode node)
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

		if(version > registry.currentVersion())
		{
			throw new MigrationException("Format version " + version + " is newer than the supported version: " + registry.currentVersion());
		}

		if(version < registry.getMinSupportedVersion())
		{
			throw new MigrationException("Format version " + version + " is too old to migrate. Minimum supported version is: " + registry.getMinSupportedVersion());
		}

		int current = version;
		while(current < registry.currentVersion())
		{
			for(final JsonMigrationStep step : registry.getStepsByTargetVersion(current + 1))
			{
				apply(root, step);
			}

			current++;
			JsonPathOperations.add(root, versionPath, new IntNode(current));
		}
		return root;
	}

	private int parseVersion(JsonNode root)
	{
		final JsonNode versionNode = root.path(versionPath);
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
			case JsonMigrationStepAdd addStep -> JsonPathOperations.add(root, addStep.path(), addStep.value());
			case JsonMigrationStepDelete deleteStep -> JsonPathOperations.remove(root, deleteStep.path());
			case JsonMigrationStepMove moveStep ->
			{
				final JsonNode value = JsonPathOperations.remove(root, moveStep.from());
				JsonPathOperations.add(root, moveStep.to(), value);
			}
			case JsonMigrationStepForEach forEachStep -> applyToEach(root, forEachStep);
		}
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
}