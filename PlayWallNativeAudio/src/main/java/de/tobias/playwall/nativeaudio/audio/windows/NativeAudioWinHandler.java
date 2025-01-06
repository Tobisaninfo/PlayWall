package de.tobias.playwall.nativeaudio.audio.windows;

import de.tobias.playwall.server.common.DurationHelper;
import de.tobias.playwall.server.common.audio.AudioHandler;
import de.tobias.playwall.server.common.audio.Seekable;
import de.tobias.playwall.server.common.audio.Soundcardable;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.pad.content.PadContent;
import nativeaudio.NativeAudio;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class NativeAudioWinHandler extends AudioHandler implements Soundcardable, Seekable
{
	static final String SOUND_CARD = "SoundCard";

	private NativeAudio audioHandler;
	private Duration duration;
	private Duration position;

	private static Thread positionThread;
	private static final List<NativeAudioWinHandler> playedHandlers = new ArrayList<>();
	private static final int SLEEP_TIME_POSITION = 50;

	static
	{
		positionThread = new Thread(() ->
		{
			while(true)
			{
				try
				{
					if(playedHandlers.isEmpty())
					{
						synchronized(positionThread)
						{
							positionThread.wait();
						}
					}

					for(Iterator<NativeAudioWinHandler> iterator = playedHandlers.iterator(); iterator.hasNext(); )
					{
						NativeAudioWinHandler handler = iterator.next();
						Pad pad = handler.getContent().getPad();

						if(handler.audioHandler != null)
						{
							if(!handler.audioHandler.isPlaying())
							{
								// TODO
//								if(!pad.getIsLoop())
//								{
//									// Remove from Loop and Stop
//									iterator.remove();
//									pad.setStatus(PadControllerStatus.EOF);
//								}
							}
						}

						if(handler.audioHandler != null)
						{
							handler.position = DurationHelper.convertMillisToDuration(handler.audioHandler.getPosition());
						}
					}

					Thread.sleep(SLEEP_TIME_POSITION);
				}
				catch(ConcurrentModificationException ignored)
				{
				}
				catch(InterruptedException e)
				{
					break;
				}
				catch(Exception e)
				{
					// TODO
				}
			}
		});

		positionThread.start();
	}

	NativeAudioWinHandler(PadContent content)
	{
		super(content);
		duration = Duration.ZERO;
		position = Duration.ZERO;
	}

	@Override
	public void play()
	{
		// TODO
//		audioHandler.setLoop(getContent().getPad().getIsLoop());

		audioHandler.play();

		boolean start = false;
		if(playedHandlers.isEmpty())
		{
			start = true;
		}

		if(!playedHandlers.contains(this))
			playedHandlers.add(this);
		if(start)
		{
			synchronized(positionThread)
			{
				positionThread.notify();
			}
		}
	}

	@Override
	public void pause()
	{
		audioHandler.pause();
		playedHandlers.remove(this);
	}

	@Override
	public void stop()
	{
		audioHandler.stop();
		playedHandlers.remove(this);
	}

	@Override
	public void seekToStart()
	{
		audioHandler.seek(0);
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
		if(audioHandler != null)
		{
			audioHandler.setVolume((float) volume);
		}
	}

	@Override
	public boolean isMediaLoaded()
	{
		return audioHandler != null;
	}

	@Override
	public void loadMedia(Path[] paths)
	{
		if(audioHandler == null)
			audioHandler = new NativeAudio();
		audioHandler.load(paths[0].toString());

		// TODO get output device name from settings
		setOutputDevice("1/2 - PC Sound (GIGAPort HD Audio driver)");

		duration = DurationHelper.convertMillisToDuration(audioHandler.getDuration());
		// TODO
//		getContent().getPad().setStatus(PadControllerStatus.READY);
	}

	@Override
	public void unloadMedia()
	{
		if(audioHandler != null)
		{
			audioHandler.unload();
			audioHandler = null;
		}
	}

	@Override
	public void setOutputDevice(String name)
	{
		audioHandler.setDevice(name);
	}
}
