package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.audio.PlatformAudioHandlerFactory;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.common.project.Pauseable;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioPadController extends PadController implements Pauseable
{
	private final PlatformAudioHandlerFactory audioHandlerFactory;
	private AudioHandler audioHandler;

	protected AudioPadController(ApplicationContext context, Pad pad, PlatformAudioHandlerFactory audioHandlerFactory)
	{
		super(context, pad);
		this.audioHandlerFactory = audioHandlerFactory;
	}

	@Override
	protected void loadInternal()
	{
		audioHandler = audioHandlerFactory.createAudioHandler(this);
		// TODO: Check for file existences
		audioHandler.loadMedia(pad.getMediaPaths().stream().map(Paths::get).toArray(Path[]::new));

		// TODO: set volume
	}

	@Override
	protected void unloadInternal()
	{
		setStatus(PadControllerStatus.STOP);
		audioHandler.unloadMedia();
	}

	@Override
	public void play(boolean withFadeIn)
	{
		audioHandler.play();
		setStatus(PadControllerStatus.PLAY);
	}

	@Override
	public void pause()
	{
		audioHandler.pause();
		setStatus(PadControllerStatus.PAUSE);
	}

	@Override
	public void stop()
	{
		audioHandler.stop();
		setStatus(PadControllerStatus.STOP);
	}
}
