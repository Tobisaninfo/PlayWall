package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadLoadedUpdate;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.common.model.project.Pad;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

@Slf4j
@Getter
@Setter
public abstract class PadController
{
	@Getter(AccessLevel.PRIVATE)
	protected final ApplicationContext context;
	protected final Pad pad;
	private PadControllerStatus status;

	protected PadController(ApplicationContext context, Pad pad)
	{
		this.context = context;
		this.pad = pad;
		this.status = PadControllerStatus.EMPTY;
	}

	public void load()
	{
		context.publishEvent(new PadLoadedUpdate(pad.getId(), false));
		log.debug("Loading Pad {}", pad.getId());
		loadInternal();
		status = PadControllerStatus.READY;
		context.publishEvent(new PadLoadedUpdate(pad.getId(), true, getDuration().toMillis()));
	}

	protected abstract void loadInternal();

	public void unload()
	{
		log.debug("Unload Pad {}", pad.getId());
		unloadInternal();
		status = PadControllerStatus.EMPTY;
	}

	protected abstract void unloadInternal();

	public abstract void play(boolean withFadeIn);

	public abstract void pause();

	public abstract void stop();

	public abstract void onEof();

	public abstract Duration getDuration();

	public abstract Duration getPlayPosition();

	// TODO: Cannot be in generic PadController
	public abstract void setVolume(double volume);

	// TODO: Cannot be in generic PadController
	public abstract void setLooping(boolean looping);
}
