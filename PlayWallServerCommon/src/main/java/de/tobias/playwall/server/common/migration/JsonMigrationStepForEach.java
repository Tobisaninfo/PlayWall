package de.tobias.playwall.server.common.migration;

/**
 * Applies the wrapped step to every element of the array at the given path. The inner step's path is
 * addressed relative to each element (e.g. {@code JsonMigrationStepForEach.of("/pads", JsonMigrationStepAdd.of("/active", false))}
 * adds an {@code active: false} property to every pad). The array must already exist; elements are never
 * auto-created. Each element must be an object.
 */
public record JsonMigrationStepForEach(String arrayPath, JsonMigrationStep step) implements JsonMigrationStep
{
	public JsonMigrationStepForEach
	{
		JsonMigrationStep.validatePath(arrayPath);

		if(step == null)
		{
			throw new IllegalArgumentException("Step must not be null");
		}
	}

	public static JsonMigrationStepForEach of(String arrayPath, JsonMigrationStep step)
	{
		return new JsonMigrationStepForEach(arrayPath, step);
	}
}