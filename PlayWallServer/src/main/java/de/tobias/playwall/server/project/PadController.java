package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import de.tobias.playwall.server.common.model.project.Pad;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PadController
{
	private final ApplicationContext context;
	private final Pad pad;

	void load()
	{
		log.debug("Loading Pad {}", pad.getId());
		_load();
		context.publishEvent(new PadLoadedUpdate(pad.getId()));
	}

	protected abstract void _load();

	void unload()
	{
		log.debug("Unload Pad {}", pad.getId());
		_unload();
	}

	protected abstract void _unload();

	public abstract void play(boolean withFadeIn);

	public abstract boolean stop();
}
