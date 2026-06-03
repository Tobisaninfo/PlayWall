package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.pad.PadControllerStatus;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.project.FadeSettings;
import de.tobias.playwall.server.common.model.project.Project;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

public class AudioPadContentController extends PadController
{
	private final AudioHandler audioHandler;

	private final Project project;
	@Getter(AccessLevel.PACKAGE)
	private final AudioPadContent padContent;

	private double currentPadVolume;
	private Thread fadeThread;

	public AudioPadContentController(ApplicationContext context, Pad pad, AudioPadContent padContent, AudioHandlerFactory audioHandlerFactory, Project project)
	{
		super(context, pad);
		this.padContent = padContent;
		this.audioHandler = audioHandlerFactory.createAudioHandler(this::onEof);
		this.project = project;
		addPlaybackListener(new EndOfFileFadeListener(this));
	}

	@Override
	protected void loadInternal() throws IOException
	{
		audioHandler.loadMedia(Paths.get(padContent.getMediaPath()));
		setVolume(padContent.getVolume());
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
		fireListeners(PlaybackListener::onPlay);
		audioHandler.setLooping(padContent.isLoop());

		if(withFadeIn)
		{
			final FadeSettings fadeSettings = getEffectiveFadeSettings();
			final boolean isPaused = getStatus() == PadControllerStatus.PAUSE;
			final boolean fadeEnabled = isPaused
					? BooleanUtils.isTrue(fadeSettings.getFadeInOnResume())
					: BooleanUtils.isTrue(fadeSettings.getFadeInOnPlay());
			if(fadeEnabled)
			{
				fadeIn(fadeSettings.getFadeInDuration());
			}
		}

		audioHandler.play();
		setStatus(PadControllerStatus.PLAY);
	}

	@Override
	public void pause()
	{
		final FadeSettings fadeSettings = getEffectiveFadeSettings();
		if(BooleanUtils.isTrue(fadeSettings.getFadeOutOnPause()))
		{
			fadeOut(fadeSettings.getFadeOutDuration(), () -> {
				audioHandler.pause();
				setStatus(PadControllerStatus.PAUSE);
			});
		}
		else
		{
			audioHandler.pause();
			setStatus(PadControllerStatus.PAUSE);
		}
	}

	@Override
	public void stop()
	{
		final FadeSettings fadeSettings = getEffectiveFadeSettings();
		if(BooleanUtils.isTrue(fadeSettings.getFadeOutOnStop()))
		{
			setStatus(PadControllerStatus.STOP);
			fadeOut(fadeSettings.getFadeOutDuration(), () -> {
				audioHandler.stop();
				setStatus(PadControllerStatus.READY);
			});
		}
		else
		{
			audioHandler.stop();
			setStatus(PadControllerStatus.STOP);
			setStatus(PadControllerStatus.READY);
		}
	}

	@Override
	public void onEof()
	{
		fireListeners(PlaybackListener::onEof);
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
		currentPadVolume = volume;

		//Apply global volume
		final double masterVolume = VolumeHelper.calculateVolume(project, volume);
		audioHandler.setVolume(masterVolume);
	}

	@Override
	public void setLooping(boolean looping)
	{
		audioHandler.setLooping(looping);
	}

	FadeSettings getEffectiveFadeSettings()
	{
		return Optional.ofNullable(pad.getFadeSettings()).orElse(project.getMetadata().getFadeSettings());
	}

	void fadeIn(double durationInSeconds)
	{
		interruptCurrentFade();
		fadeThread = Thread.ofVirtual().start(new FadeController(this, 0, padContent.getVolume(), secondsToDuration(durationInSeconds)));
	}

	void fadeOut(double durationInSeconds, Runnable onFadeFinished)
	{
		interruptCurrentFade();
		fadeThread = Thread.ofVirtual().start(new FadeController(this, currentPadVolume, 0, secondsToDuration(durationInSeconds), new FadeController.FadeControllerListener()
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

	static Duration secondsToDuration(double durationInSeconds)
	{
		return Duration.ofMillis((long) (durationInSeconds * 1000));
	}
}
