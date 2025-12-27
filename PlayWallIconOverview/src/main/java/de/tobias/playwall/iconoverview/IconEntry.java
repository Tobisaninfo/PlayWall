package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontIconType;

import java.util.List;

public record IconEntry(FontIconType fontIconType, List<IconUsage> usages)
{
	public String getDescription()
	{
		final StringBuilder builder = new StringBuilder();
		for(IconUsage usage : usages)
		{
			builder.append(usage.category().getName());
			builder.append(" - ");
			builder.append(usage.description());
			builder.append(" ");
		}

		return builder.toString();
	}
}
