package de.tobias.playwall.client.domain.pad;

import de.tobias.playwall.common.api.pad.PadControllerStatus;

public enum PadStatus
{
	EMPTY,
	ERROR,
	READY,
	PLAY,
	PAUSE;

	public static PadStatus fromPadControllerStatus(PadControllerStatus status)
	{
		return switch(status)
		{
			case EMPTY -> PadStatus.EMPTY;
			case ERROR -> PadStatus.ERROR;
			case READY, STOP, EOF -> PadStatus.READY;
			case PLAY -> PadStatus.PLAY;
			case PAUSE -> PadStatus.PAUSE;
		};
	}
}
