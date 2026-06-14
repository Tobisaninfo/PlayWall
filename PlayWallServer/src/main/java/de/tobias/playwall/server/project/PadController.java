package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.common.api.pad.update.PadLoadedUpdate;
import de.tobias.playwall.common.api.pad.update.PadStatusUpdate;
import de.tobias.playwall.server.common.model.pad.Pad;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
@Getter
@Setter
public abstract class PadController
{
	@Getter(AccessLevel.PRIVATE)
	protected final ApplicationContext context;
	protected final Pad pad;
	private PadControllerStatus status;

	private final List<PlaybackListener> playbackListeners = new CopyOnWriteArrayList<>();

	protected PadController(ApplicationContext context, Pad pad)
	{
		this.context = context;
		this.pad = pad;
		this.status = PadControllerStatus.EMPTY;
	}

	public void setStatus(PadControllerStatus status)
	{
		this.status = status;
		this.context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}

	public void load()
	{
		try
		{
			context.publishEvent(new PadLoadedUpdate(pad.getId(), false));
			log.debug("Loading Pad {}", pad.getId());
			loadInternal();
			setStatus(PadControllerStatus.READY);
			context.publishEvent(new PadLoadedUpdate(pad.getId(), true, getDuration().toMillis()));
		}
		catch(IOException e)
		{
			setStatus(PadControllerStatus.ERROR);
			context.publishEvent(new PadLoadedUpdate(pad.getId(), true, null));
			log.error("Cannot load pad", e);
		}
	}

	protected abstract void loadInternal() throws IOException;

	public void unload()
	{
		stopImmediately();
		log.debug("Unload Pad {}", pad.getId());
		unloadInternal();
		setStatus(PadControllerStatus.EMPTY);
	}

	protected abstract void unloadInternal();

	public abstract void play() throws IOException;

	public abstract void setStartPosition(Duration startPosition);

	public abstract void setEndPosition(Duration endPosition);

	public abstract void clearEndPosition();

	public abstract void seekToStart();

	public abstract void seekTo(Duration position);

	public abstract void setPlaybackSpeed(double speed);

	public abstract void pause();

	public abstract void stop();

	public abstract void stopImmediately();

	public abstract void onEof();

	public abstract Duration getDuration();

	public abstract Duration getPlayPosition();

	// TODO: Cannot be in generic PadController
	public abstract void setVolume(double volume);

	// TODO: Cannot be in generic PadController
	public abstract void setLooping(boolean looping);

	public void addPlaybackListener(PlaybackListener listener)
	{
		playbackListeners.add(listener);
	}

	public void removePlaybackListener(PlaybackListener listener)
	{
		playbackListeners.remove(listener);
	}

	public void fireListeners(Consumer<PlaybackListener> listenerConsumer)
	{
		playbackListeners.forEach(listenerConsumer);
	}

	void notifyPlaybackPositionListeners(Duration position, Duration duration)
	{
		playbackListeners.forEach(l -> l.onPositionUpdate(position, duration));
	}

	public abstract void setOutputDevice(String audioDeviceName);
}
