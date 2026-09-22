package de.tobias.playwall.server.common.migration;

import java.util.*;

/**
 * Registry of migration steps keyed by the format version they produce. A registration for version
 * {@code v} describes what has to change to turn a JSON node of version {@code v - 1} into version {@code v}.
 *
 * <p>Supported versions must be contiguous: every version between the first registered migration and
 * {@link #currentVersion()} has to be registered (empty step lists are allowed) so that any supported
 * JSON node can be migrated to the latest version by applying the migrations in order.
 */
public final class MigrationRegistry
{
	private final int currentVersion;
	private final SortedMap<Integer, List<JsonMigrationStep>> steps;

	private MigrationRegistry(int currentVersion, SortedMap<Integer, List<JsonMigrationStep>> steps)
	{
		this.currentVersion = currentVersion;
		this.steps = Collections.unmodifiableSortedMap(steps);
	}

	public static Builder builder(int currentVersion)
	{
		if(currentVersion < 1)
		{
			throw new IllegalArgumentException("currentVersion must be at least 1: " + currentVersion);
		}
		return new Builder(currentVersion);
	}

	public int currentVersion()
	{
		return currentVersion;
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

	public static final class Builder
	{
		private final int currentVersion;
		private final SortedMap<Integer, List<JsonMigrationStep>> steps = new TreeMap<>();

		private Builder(int currentVersion)
		{
			this.currentVersion = currentVersion;
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
			if(steps.isEmpty())
			{
				return new MigrationRegistry(currentVersion, new TreeMap<>());
			}

			final int first = steps.firstKey();
			for(int target = first; target <= currentVersion; target++)
			{
				if(!steps.containsKey(target))
				{
					throw new IllegalArgumentException("Missing migration for format version " + target);
				}
			}

			return new MigrationRegistry(currentVersion, new TreeMap<>(steps));
		}
	}
}