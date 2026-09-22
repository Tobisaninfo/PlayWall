package de.tobias.playwall.server.common.migration;

/**
 * Deletes the node at the given path. The path must exist, otherwise a {@link MigrationException} is thrown.
 */
public record JsonMigrationStepDelete(String path) implements JsonMigrationStep
{
	public JsonMigrationStepDelete
	{
		JsonMigrationStep.validatePath(path);
	}
}