package de.tobias.playwall.server.common.model.project;

import java.util.ArrayDeque;
import java.util.UUID;

public class RecentProjectsStack extends ArrayDeque<UUID>
{
	private static final Integer MAX_SIZE = 5;

	@Override
	public void push(UUID item)
	{
		// remove existing occurrence (if present) to avoid duplicates
		remove(item);

		super.push(item);

		while(this.size() > MAX_SIZE)
		{
			this.removeLast();
		}
	}
}
