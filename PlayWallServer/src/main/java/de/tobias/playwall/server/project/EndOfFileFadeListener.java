package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.FadeSettings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.time.Duration;

import static de.tobias.playwall.server.project.AudioPadContentController.secondsToDuration;

@RequiredArgsConstructor
class EndOfFileFadeListener implements PlaybackListener
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

		final Duration fadeOutDuration = secondsToDuration(fadeSettings.getFadeOutDuration());
		final Duration remaining = duration.minus(position);
		if(remaining.compareTo(fadeOutDuration) <= 0)
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
