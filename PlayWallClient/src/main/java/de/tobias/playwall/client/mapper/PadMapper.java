package de.tobias.playwall.client.mapper;

import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.common.api.project.model.PadDto;

public class PadMapper
{
	public Pad padDtoToPad(PadDto pad)
	{
		return new Pad(pad.id(), pad.name(), pad.position(), pad.status(), pad.mediaPaths());
	}
}
