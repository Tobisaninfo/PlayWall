package de.tobias.playwall.server.project;

import de.tobias.playwall.server.api.project.model.Pad;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PadController
{
	private final Pad pad;

	public PadController(Pad pad)
	{
		this.pad = pad;
	}

	void load()
	{
		log.debug("Loading Pad {}", pad.getId());
		// TODO: Load media
	}

	void unload()
	{
		log.debug("Unload Pad {}", pad.getId());
		// TODO: Unload media
	}
}
