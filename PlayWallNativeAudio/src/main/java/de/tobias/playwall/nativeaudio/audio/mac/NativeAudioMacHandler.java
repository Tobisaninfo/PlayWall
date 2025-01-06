package de.tobias.playwall.nativeaudio.audio.mac;

import de.thecodelabs.utils.threading.Worker;
import de.tobias.playwall.common.api.project.model.PadControllerStatus;
import de.tobias.playwall.server.common.DurationHelper;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.Peakable;
import de.tobias.playwall.server.common.audio.Seekable;
import de.tobias.playwall.server.common.audio.Soundcardable;
import de.tobias.playwall.server.common.project.PadController;
import lombok.Setter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public class NativeAudioMacHandler extends AudioHandler implements Peakable, Seekable, Soundcardable
{
	public static final String SOUND_CARD = "SoundCardMac";

	@Setter
	private Duration position;
	private Duration duration;
	private boolean isLoaded;

	private double leftPeak;
	private double rightPeak;

	private final AVAudioPlayerBridge bridge;

	NativeAudioMacHandler(PadController padController)
	{
		super(padController);

		bridge = new AVAudioPlayerBridge();

		position = Duration.ZERO;
		duration = Duration.ZERO;

		leftPeak = 0.0;
		rightPeak = 0.0;
	}

	AVAudioPlayerBridge getBridge()
	{
		return bridge;
	}

	@Override
	public void play()
	{
		// TODO
//		bridge.setLoop(getContent().getPad().getIsLoop());
		bridge.play();
	}

	@Override
	public void pause()
	{
		bridge.pause();
	}

	@Override
	public void stop()
	{
		bridge.stop();
	}

	@Override
	public void seekToStart()
	{
		bridge.seek(0);
	}

	@Override
	public Duration getPosition()
	{
		return position;
	}

	@Override
	public Duration getDuration()
	{
		return duration;
	}

	@Override
	public void setVolume(double volume)
	{
		bridge.setVolume(volume);
	}

	@Override
	public boolean isMediaLoaded()
	{
		return isLoaded;
	}

	@Override
	public void loadMedia(Path[] paths)
	{
		Worker.runLater(() ->
		{
			isLoaded = bridge.load(paths[0].toString());
			if(isLoaded)
			{
				// TODO get output device name from settings
				setOutputDevice("USB Audio Device");

				duration = DurationHelper.convertMillisToDuration(bridge.getDuration());
				getController().setStatus(PadControllerStatus.READY);
				// TODO
//				getController().updateVolume();
			}
		});
	}

	@Override
	public void unloadMedia()
	{
		bridge.dispose();
	}

	public void setAudioLevel(Channel channel, double level)
	{
		if(channel == Channel.LEFT)
		{
			leftPeak = level;
		}
		else if(channel == Channel.RIGHT)
		{
			rightPeak = level;
		}

		throw new IllegalArgumentException("Unsupported channel: " + channel);
	}

	@Override
	public Double audioLevel(Channel channel)
	{
		if(channel == Channel.LEFT)
		{
			return leftPeak;
		}
		else if(channel == Channel.RIGHT)
		{
			return rightPeak;
		}
		return null;
	}

	@Override
	public double getAudioLevel(Channel channel)
	{
		return audioLevel(channel);
	}

	private AudioDevice[] devices;

	@Override
	public void setOutputDevice(String name)
	{
		if(devices == null)
		{
			devices = AVAudioPlayerBridge.getAudioDevices();
		}

		final Optional<String> first = Stream.of(devices)
				.filter(device -> device.name().equals(name))
				.map(AudioDevice::id)
				.findFirst();
		first.ifPresent(bridge::setCurrentAudioDevice);
	}
}
