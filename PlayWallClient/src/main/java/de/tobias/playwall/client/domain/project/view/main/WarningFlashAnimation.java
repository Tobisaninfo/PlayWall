package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.jfx.ColorUtils;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.view.desktop.DesktopPadView;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
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

	private final ObjectProperty<Color> flashColor = new SimpleObjectProperty<>();
	private Timeline toggleTimeline;

	public void init(DesktopPadView padView)
	{
		this.padView = padView;
		flashColor.addListener((_, _, newColor) -> applyColor(newColor));
	}

	public void start()
	{
		stop();
		log.trace("Start warning animation");

		final ClientPadController padController = padView.getPadController();
		final Color fromColor = padController.getEffectiveDefaultColor().getColor();
		final Color toColor = padController.getEffectivePlayColor().getColor();

		toggleTimeline = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(flashColor, fromColor, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(200), new KeyValue(flashColor, fromColor, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(300), new KeyValue(flashColor, toColor, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(700), new KeyValue(flashColor, toColor, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(800), new KeyValue(flashColor, fromColor, Interpolator.EASE_BOTH)),
				new KeyFrame(Duration.millis(1000), new KeyValue(flashColor, fromColor, Interpolator.EASE_BOTH))
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
			flashColor.set(null);
		}
	}

	public boolean isRunning()
	{
		return toggleTimeline != null;
	}

	public Color getCurrentColor()
	{
		return flashColor.get();
	}

	private void applyColor(Color color)
	{
		padView.getRootNode().setStyle(color == null ? "" : "-fx-background-color: " + toCssColor(color) + ";");
	}

	private static String toCssColor(Color color)
	{
		return ColorUtils.toRGBHex(color);
	}
}
