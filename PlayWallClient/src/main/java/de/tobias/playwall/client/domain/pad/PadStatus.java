package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.common.api.pad.PadControllerStatus;

public enum PadStatus
{
	EMPTY,
	ERROR,
	READY,
	PLAYING,
	PAUSING,
	PAUSED,
	STOPPING,
	STOPPED;

	public boolean isAnyPlayingState()
	{
		return this == PLAYING || this == PAUSING || this == STOPPING;
	}

	public boolean isPaused()
	{
		return this == PAUSED;
	}

	public static PadStatus fromPadControllerStatus(PadControllerStatus status)
	{
		return switch(status)
		{
			case EMPTY -> PadStatus.EMPTY;
			case ERROR -> PadStatus.ERROR;
			case READY, EOF -> PadStatus.READY;
			case PLAYING -> PadStatus.PLAYING;
			case STOPPING -> PadStatus.STOPPING;
			case STOPPED -> PadStatus.STOPPED;
			case PAUSING -> PadStatus.PAUSING;
			case PAUSED -> PadStatus.PAUSED;
		};
	}
}
