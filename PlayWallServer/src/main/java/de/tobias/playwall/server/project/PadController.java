package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import de.tobias.playwall.server.common.model.project.Pad;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class PadController
{
	private final ApplicationContext context;
	private final Pad pad;

	public PadController(ApplicationContext context, Pad pad)
	{
		this.context = context;
		this.pad = pad;
	}

	void load()
	{
		log.debug("Loading Pad {}", pad.getId());
		// TODO: Load media
		context.publishEvent(new PadLoadedUpdate(pad.getId()));
	}

	void unload()
	{
		log.debug("Unload Pad {}", pad.getId());
		// TODO: Unload media
	}
}
