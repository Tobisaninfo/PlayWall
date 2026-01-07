package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadStatusUpdate;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import org.springframework.context.ApplicationContext;

import java.nio.file.Paths;
import java.time.Duration;

public class AudioPadContentController extends PadController
{
	private final AudioHandlerFactory audioHandlerFactory;
	private AudioHandler audioHandler;

	private final AudioPadContent padContent;

	protected AudioPadContentController(ApplicationContext context, Pad pad, AudioPadContent padContent, AudioHandlerFactory audioHandlerFactory)
	{
		super(context, pad);
		this.padContent = padContent;
		this.audioHandlerFactory = audioHandlerFactory;
	}

	@Override
	protected void loadInternal()
	{
		audioHandler = audioHandlerFactory.createAudioHandler(this::onEof);
		audioHandler.loadMedia(Paths.get(padContent.getMediaPath()));
		audioHandler.setVolume(padContent.getVolume());
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
		audioHandler.setLooping(padContent.isLoop());
		audioHandler.play();
		setStatus(PadControllerStatus.PLAY);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}

	@Override
	public void pause()
	{
		audioHandler.pause();
		setStatus(PadControllerStatus.PAUSE);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}

	@Override
	public void stop()
	{
		audioHandler.stop();
		setStatus(PadControllerStatus.STOP);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
		setStatus(PadControllerStatus.READY);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}

	@Override
	public void onEof()
	{
		setStatus(PadControllerStatus.EOF);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
		setStatus(PadControllerStatus.READY);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}

	@Override
	public Duration getPlayPosition()
	{
		return audioHandler.getPosition();
	}

	@Override
	public void setVolume(double volume)
	{
		audioHandler.setVolume(volume);
	}

	@Override
	public void setLooping(boolean looping)
	{
		audioHandler.setLooping(looping);
	}
}
