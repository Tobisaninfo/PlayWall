package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.FadeSettings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

@RequiredArgsConstructor
class AudioPadEndOfFileFadeListener implements PlaybackListener
{
	private final AudioPadContentController controller;
	private boolean eofFadeTriggered = false;

	@Override
	public void onPositionUpdate(Duration position, Duration duration)
	{
		if(controller.getPadContent().isLoop() || eofFadeTriggered || duration == null || duration.isZero())
		{
			return;
		}

		final FadeSettings fadeSettings = controller.getEffectiveFadeSettings();
		if(BooleanUtils.isNotTrue(fadeSettings.getFadeOutOnEndOfFile()))
		{
			return;
		}

		final long fadeOutDuration = (long) (fadeSettings.getFadeOutDuration() * 1000);
		final long remaining = duration.minus(position).toMillis();
		if(remaining <= fadeOutDuration)
		{
			eofFadeTriggered = true;

			controller.fadeOut(fadeSettings.getFadeOutDuration(), () -> {});
		}
	}

	@Override
	public void onPlay()
	{
		eofFadeTriggered = false;
	}

	@Override
	public void onEof()
	{
		eofFadeTriggered = false;
	}
}
