package de.tobias.playwall.server.common.migration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Adds a value at the given path inside the object at {@code objectPath}, but only if that object already exists
 * and its type property matches the expected type. Objects that are missing or {@code null}, as well as objects of
 * another type, are skipped and never auto-created.
 */
public record JsonMigrationStepAddToTypedObject(String objectPath, String typeProperty, String typeValue, String path,
                                                JsonNode value)
		implements JsonMigrationStep
{
	public JsonMigrationStepAddToTypedObject(String objectPath, String typeProperty, String typeValue, String path, JsonNode value)
	{
		JsonMigrationStep.validatePath(objectPath);
		JsonMigrationStep.validatePath(typeProperty);
		JsonMigrationStep.validatePath(path);

		if(typeValue == null || typeValue.isBlank())
		{
			throw new IllegalArgumentException("Type value must not be blank: " + typeValue);
		}

		this.objectPath = objectPath;
		this.typeProperty = typeProperty;
		this.typeValue = typeValue;
		this.path = path;
		this.value = value == null ? JsonNodeFactory.instance.nullNode() : value;
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, JsonNode value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, value);
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, String value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, JsonNodeFactory.instance.stringNode(value));
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, boolean value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, JsonNodeFactory.instance.booleanNode(value));
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, int value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, JsonNodeFactory.instance.numberNode(value));
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, long value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, JsonNodeFactory.instance.numberNode(value));
	}

	public static JsonMigrationStepAddToTypedObject of(String objectPath, String typeProperty, String typeValue, String path, double value)
	{
		return new JsonMigrationStepAddToTypedObject(objectPath, typeProperty, typeValue, path, JsonNodeFactory.instance.numberNode(value));
	}
}
