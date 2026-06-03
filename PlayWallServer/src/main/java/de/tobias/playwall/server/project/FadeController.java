package de.tobias.playwall.server.project;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	private final double seconds;
	private FadeControllerListener listener;

	@Override
	public void run()
	{
		final long totalMillis = (long) (seconds * 1000);
		final double delta = to - from;
		log.debug("Fade from {} to {} for {}ms", from, to, totalMillis);

		long startTime = System.currentTimeMillis();
		controller.setVolume(from);

		if(listener != null)
		{
			listener.onFadeStart();
		}

		boolean running = true;
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
