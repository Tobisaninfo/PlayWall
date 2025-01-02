package de.tobias.playwall.server.common.audio;


import de.tobias.playwall.server.common.model.project.Pad;

import java.time.Duration;

public interface Durationable
{
	Duration getDuration();

	Duration getPosition();

	default Duration getRemaining(Pad pad)
	{
		if(!pad.getIsLoop())
		{
			final Duration position = getPosition();
			final Duration duration = getDuration();

			if(position != null && duration != null)
			{
				return duration.minus(position);
			}
		}
		return null;
	}
}
