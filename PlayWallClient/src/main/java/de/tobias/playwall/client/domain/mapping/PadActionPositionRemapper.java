package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.input.InputKey;
import de.tobias.playwall.client.domain.mapping.action.PadAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PadActionPositionRemapper
{
	public static void remapPositions(Map<UUID, Mapping> mappings, int oldNumberOfHorizontalPads, int newNumberOfHorizontalPads, int newNumberOfVerticalPads)
	{
		for(final Mapping mapping : mappings.values())
		{
			for(final InputKey inputKey : mapping.getAllInputKeys())
			{
				if(mapping.getAction(inputKey) instanceof PadAction padAction)
				{
					remapPosition(padAction, oldNumberOfHorizontalPads, newNumberOfHorizontalPads, newNumberOfVerticalPads);
				}
			}
		}
	}

	static void remapPosition(PadAction padAction, int oldNumberOfHorizontalPads, int newNumberOfHorizontalPads, int newNumberOfVerticalPads)
	{
		final Integer position = padAction.getPosition();
		if(position == null)
		{
			return;
		}

		final int row = position / oldNumberOfHorizontalPads;
		final int column = position % oldNumberOfHorizontalPads;

		if(row < newNumberOfVerticalPads && column < newNumberOfHorizontalPads)
		{
			padAction.setPosition(row * newNumberOfHorizontalPads + column);
		}
		else
		{
			padAction.setPosition(null);
		}
	}
}
