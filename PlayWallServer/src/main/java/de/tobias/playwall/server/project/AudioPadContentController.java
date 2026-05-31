package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

public class AudioPadContentController extends PadController
{
	private final AudioHandler audioHandler;

	private final AudioPadContent padContent;

	private Thread fadeThread;

	protected AudioPadContentController(ApplicationContext context, Pad pad, AudioPadContent padContent, AudioHandlerFactory audioHandlerFactory)
	{
		super(context, pad);
		this.padContent = padContent;
		this.audioHandler = audioHandlerFactory.createAudioHandler(this::onEof);
	}

	@Override
	protected void loadInternal() throws IOException
	{
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
	public void play(boolean withFadeIn) throws IOException
	{
		audioHandler.setLooping(padContent.isLoop());
		fadeIn();
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
		fadeOut(() -> {
			audioHandler.stop();
			setStatus(PadControllerStatus.STOP);
			setStatus(PadControllerStatus.READY);
		});
	}

	@Override
	public void onEof()
	{
		setStatus(PadControllerStatus.EOF);
		setStatus(PadControllerStatus.READY);
	}

	@Override
	public Duration getDuration()
	{
		return audioHandler.getDuration();
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

	private void fadeIn()
	{
		interruptCurrentFade();
		fadeThread = Thread.ofVirtual().start(new FadeController(this, 0, padContent.getVolume(), Duration.ofSeconds(5)));
	}

	private void fadeOut(Runnable onFadeFinished)
	{
		interruptCurrentFade();
		fadeThread = Thread.ofVirtual().start(new FadeController(this, audioHandler.getVolume(), 0, Duration.ofSeconds(5), new FadeController.FadeControllerListener()
		{
			@Override
			public void onFadeFinished()
			{
				onFadeFinished.run();
			}
		}));
	}

	private void interruptCurrentFade()
	{
		if(fadeThread != null)
		{
			fadeThread.interrupt();
		}
	}
}
