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

	private final String initialOutputDeviceName;

	private double currentPadVolume;
	private Thread fadeThread;

	public AudioPadContentController(ApplicationContext context, Pad pad, AudioPadContent padContent, AudioHandlerFactory audioHandlerFactory, Project project, String initialOutputDeviceName)
	{
		super(context, pad);
		this.padContent = padContent;
		this.audioHandler = audioHandlerFactory.createAudioHandler(this::onEof);
		this.initialOutputDeviceName = initialOutputDeviceName;
		this.project = project;
		addPlaybackListener(new AudioPadEndOfFileFadeListener(this));
	}

	@Override
	protected void loadInternal() throws IOException
	{
		if(initialOutputDeviceName != null)
		{
			audioHandler.setOutputDevice(initialOutputDeviceName);
		}
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
	public void play() throws IOException
	{
		fireListeners(PlaybackListener::onPlay);
		audioHandler.setLooping(padContent.isLoop());

		final FadeSettings fadeSettings = getEffectiveFadeSettings();
		final boolean isPaused = getStatus() == PadControllerStatus.PAUSE;
		final boolean fadeEnabled = isPaused
				? BooleanUtils.isTrue(fadeSettings.getFadeInOnResume())
				: BooleanUtils.isTrue(fadeSettings.getFadeInOnPlay());
		if(fadeEnabled)
		{
			fadeIn(fadeSettings.getFadeInDuration());
		}
		else
		{
			interruptCurrentFade();
			setVolume(padContent.getVolume());
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
			setStatus(PadControllerStatus.PAUSE);
			fadeOut(fadeSettings.getFadeOutDuration(), audioHandler::pause);
		}
		else
		{
			interruptCurrentFade();
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
			interruptCurrentFade();
			audioHandler.stop();
			setStatus(PadControllerStatus.STOP);
			setStatus(PadControllerStatus.READY);
		}
	}

	@Override
	public void stopImmediately()
	{
		interruptCurrentFade();
		audioHandler.stop();
		setStatus(PadControllerStatus.STOP);
		setStatus(PadControllerStatus.READY);
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
		fadeThread = Thread.ofVirtual().start(new FadeController(this, 0, padContent.getVolume(), durationInSeconds));
	}

	void fadeOut(double durationInSeconds, Runnable onFadeFinished)
	{
		interruptCurrentFade();
		fadeThread = Thread.ofVirtual().start(new FadeController(this, currentPadVolume, 0, durationInSeconds, new FadeController.FadeControllerListener()
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

	public void setOutputDevice(String audioDeviceName)
	{
		audioHandler.setOutputDevice(audioDeviceName);
	}
}
