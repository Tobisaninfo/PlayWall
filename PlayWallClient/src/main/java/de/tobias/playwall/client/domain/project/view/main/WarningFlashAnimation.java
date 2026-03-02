package de.tobias.playwall.client.domain.project.view.main;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.util.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service(singleton = false)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class WarningFlashAnimation
{
	private DesktopPadView padView;
	private PseudoClass pseudoClass;

	private Timeline toggleTimeline;

	public void init(DesktopPadView padView, PseudoClass pseudoClass)
	{
		this.padView = padView;
		this.pseudoClass = pseudoClass;
	}

	public void start()
	{
		stop();
		log.trace("Start warning animation");

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
		log.trace("Stop warning animation");
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
