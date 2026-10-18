package de.tobias.playwall.server.common.migration;

/**
 * Applies the wrapped step to every property value of the object at the given path, regardless of the (dynamic)
 * property names, e.g. UUID keys. If a value is an object, the step is applied to it directly. If a value is an
 * array, the step is applied to each element of the array. The inner step's path is addressed relative to that
 * object/element. If the object does not exist, the step is skipped; values are never auto-created. If it exists but is not an
 * object, the migration fails.
 *
 * <p>Example: {@code JsonMigrationStepForEachValue.of("/metadata/mappings", JsonMigrationStepAdd.of("/enabled", true))}
 * adds {@code enabled: true} to every mapping entry of every (UUID-keyed) mapping list.
 */
public record JsonMigrationStepForEachValue(String objectPath, JsonMigrationStep step) implements JsonMigrationStep
{
	public JsonMigrationStepForEachValue
	{
		JsonMigrationStep.validatePath(objectPath);

		if(step == null)
		{
			throw new IllegalArgumentException("Step must not be null");
		}
	}

	public static JsonMigrationStepForEachValue of(String objectPath, JsonMigrationStep step)
	{
		return new JsonMigrationStepForEachValue(objectPath, step);
	}
}
