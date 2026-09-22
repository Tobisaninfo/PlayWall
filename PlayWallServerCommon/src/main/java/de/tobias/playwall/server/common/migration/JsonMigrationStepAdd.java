package de.tobias.playwall.server.common.migration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Adds a value (with optional default value) at the given path. Intermediate objects on the path are created as needed.
 */
public record JsonMigrationStepAdd(String path, JsonNode value) implements JsonMigrationStep
{
	public JsonMigrationStepAdd(String path, JsonNode value)
	{
		JsonMigrationStep.validatePath(path);

		this.path = path;
		this.value = value == null ? JsonNodeFactory.instance.nullNode() : value;
	}

	public static JsonMigrationStepAdd of(String path, JsonNode value)
	{
		return new JsonMigrationStepAdd(path, value);
	}

	public static JsonMigrationStepAdd of(String path, String value)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.stringNode(value));
	}

	public static JsonMigrationStepAdd of(String path, boolean value)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.booleanNode(value));
	}

	public static JsonMigrationStepAdd of(String path, int value)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.numberNode(value));
	}

	public static JsonMigrationStepAdd of(String path, long value)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.numberNode(value));
	}

	public static JsonMigrationStepAdd of(String path, double value)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.numberNode(value));
	}

	public static JsonMigrationStepAdd nullValue(String path)
	{
		return new JsonMigrationStepAdd(path, JsonNodeFactory.instance.nullNode());
	}
}