package de.tobias.playwall.server.common.migration;

/**
 * Moves the whole node (including nested objects) from {@code from} to {@code to}.
 * The source path must exist. Moving a node into itself is rejected.
 */
public record JsonMigrationStepMove(String from, String to) implements JsonMigrationStep
{
	public JsonMigrationStepMove
	{
		JsonMigrationStep.validatePath(from);
		JsonMigrationStep.validatePath(to);

		if(from.equals(to))
		{
			throw new IllegalArgumentException("Cannot move path onto itself: " + from);
		}
	}
}