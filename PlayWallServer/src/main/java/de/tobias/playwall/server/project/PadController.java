package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.common.model.project.Pad;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
@Getter
@Setter
public abstract class PadController
{
	private final ApplicationContext context;
	private final Pad pad;
	private PadControllerStatus status;

	protected PadController(ApplicationContext context, Pad pad)
	{
		this.context = context;
		this.pad = pad;
		this.status = PadControllerStatus.EMPTY;
	}

	void load()
	{
		log.debug("Loading Pad {}", pad.getId());
		_load();
		status = PadControllerStatus.READY;
		context.publishEvent(new PadLoadedUpdate(pad.getId()));
	}

	protected abstract void _load();

	void unload()
	{
		log.debug("Unload Pad {}", pad.getId());
		_unload();
		status = PadControllerStatus.EMPTY;
	}

	protected abstract void _unload();

	public abstract void play(boolean withFadeIn);

	public abstract boolean stop();
}
