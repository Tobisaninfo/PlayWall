package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.logger.Logger;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.util.Duration;

public class WarningFlashAnimation
{
	private final DesktopPadView padView;
	private final PseudoClass pseudoClass;

	private Timeline toggleTimeline;

	public WarningFlashAnimation(DesktopPadView padView, PseudoClass pseudoClass)
	{
		this.padView = padView;
		this.pseudoClass = pseudoClass;
	}

	public void start()
	{
		stop();
		Logger.trace("Start warning animation");

		toggleTimeline = new Timeline(
				new KeyFrame(Duration.ZERO, e -> padView.pseudoClassStateChanged(pseudoClass, false)),
				new KeyFrame(Duration.millis(500), e -> padView.pseudoClassStateChanged(pseudoClass, true)),
				new KeyFrame(Duration.millis(1000), e -> padView.pseudoClassStateChanged(pseudoClass, false))
		);
		toggleTimeline.setCycleCount(Animation.INDEFINITE);
		toggleTimeline.play();
	}

	public void stop()
	{
		Logger.trace("Stop warning animation");
		if(toggleTimeline != null)
		{
			toggleTimeline.stop();
			toggleTimeline = null;
		}
		padView.pseudoClassStateChanged(pseudoClass, false);
	}

	public boolean isRunning()
	{
		return toggleTimeline != null;
	}
}
