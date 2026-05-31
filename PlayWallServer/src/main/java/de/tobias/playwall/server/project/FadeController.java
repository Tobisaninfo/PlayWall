package de.tobias.playwall.server.project;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@RequiredArgsConstructor
@AllArgsConstructor
@Slf4j
public class FadeController implements Runnable
{
	public interface FadeControllerListener
	{
		default void onFadeStart()
		{
		}

		default void onFadeFinished()
		{
		}

		default void onFadeInterrupted()
		{
		}
	}

	private final AudioPadContentController controller;
	private final double from;
	private final double to;
	private final Duration duration;
	private FadeControllerListener listener;

	@Override
	public void run()
	{
		log.debug("Fade from {} to {} for {}ms", from, to, duration.toMillis());
		long startTime = System.currentTimeMillis();
		controller.setVolume(from);

		boolean running = true;

		final long totalMillis = duration.toMillis();
		final double delta = to - from;

		if(listener != null)
		{
			listener.onFadeStart();
		}

		while(running)
		{
			try
			{
				long passedTime = System.currentTimeMillis() - startTime;
				double progress = Math.clamp((double) passedTime / totalMillis, 0, 1);

				controller.setVolume(from + progress * delta);

				if(progress >= 1)
				{
					if(listener != null)
					{
						listener.onFadeFinished();
					}
					return;
				}

				Thread.sleep(20);
			}
			catch(InterruptedException _)
			{
				running = false;
				if(listener != null)
				{
					listener.onFadeInterrupted();
				}
				controller.setVolume(to);
				Thread.currentThread().interrupt();
			}
		}
	}
}
