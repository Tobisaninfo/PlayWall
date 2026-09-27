package de.tobias.playwall.server.common.migration;


public sealed interface JsonMigrationStep permits JsonMigrationStepAdd, JsonMigrationStepAddToTypedObject, JsonMigrationStepDelete, JsonMigrationStepForEach, JsonMigrationStepMove
{
	static void validatePath(String path)
	{
		if(path == null || path.isBlank() || !path.startsWith("/"))
		{
			throw new IllegalArgumentException("Path must be a non-empty JSON pointer starting with '/': " + path);
		}
	}
}