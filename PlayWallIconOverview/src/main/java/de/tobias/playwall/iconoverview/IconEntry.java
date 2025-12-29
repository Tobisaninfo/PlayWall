package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontIconType;
import lombok.*;

import java.util.List;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class IconEntry
{
	private final FontIconType fontIconType;
	private final List<IconUsage> usages;

	@Setter
	private boolean isUnused;

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
