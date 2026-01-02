package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadStatusUpdate;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import org.springframework.context.ApplicationContext;

import java.nio.file.Paths;

public class AudioPadContentController extends PadController
{
	private final AudioHandlerFactory audioHandlerFactory;
	private AudioHandler audioHandler;

	private AudioPadContent padContent;

	protected AudioPadContentController(ApplicationContext context, Pad pad, AudioPadContent padContent, AudioHandlerFactory audioHandlerFactory)
	{
		super(context, pad);
		this.padContent = padContent;
		this.audioHandlerFactory = audioHandlerFactory;
	}

	@Override
	protected void loadInternal()
	{
		audioHandler = audioHandlerFactory.createAudioHandler(this);
		audioHandler.loadMedia(Paths.get(padContent.getMediaPath()));

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
	}

	@Override
	public void onEof()
	{
		setStatus(PadControllerStatus.EOF);
		context.publishEvent(new PadStatusUpdate(pad.getId(), getStatus()));
	}
}
