package de.tobias.playwall.client.view.components;

import java.util.Arrays;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;
import javafx.stage.Window;
import javafx.util.Duration;

public class LoadingSpinner extends StackPane
{
	private static final Duration LAP_DURATION = Duration.seconds(1.6);

	private static final Duration COLOR_FADE_DURATION = Duration.millis(400);
	public static final String DEFAULT_COLOR = "#6366F1";

	private final Arc arc = new Arc();
	private final Rotate rotateTransform = new Rotate(0, 0, 0);
	private final Timeline rotator;
	private final Timeline stretcher;
	private final Timeline colorFade = new Timeline();

	private Window window;
	private final ChangeListener<Boolean> showingListener = (_, _, showing) -> {
		if(Boolean.TRUE.equals(showing))
		{
			play();
		}
		else
		{
			stop();
		}
	};
	private final ChangeListener<Window> windowChangeListener = (_, _, newWindow) -> attachWindow(newWindow);

	private final DoubleProperty size = new SimpleDoubleProperty(this, "size", 48);
	private final DoubleProperty strokeWidth = new SimpleDoubleProperty(this, "strokeWidth", 4);

	private final ObjectProperty<Color> currentColor =
			new SimpleObjectProperty<>(this, "currentColor", Color.web(DEFAULT_COLOR));

	private final ListProperty<Color> colors =
			new SimpleListProperty<>(this, "colors", FXCollections.observableArrayList(Color.web(DEFAULT_COLOR)));

	private int colorIndex = 0;

	public LoadingSpinner()
	{
		this(48, Color.web(DEFAULT_COLOR));
	}

	public LoadingSpinner(double size)
	{
		this(size, Color.web(DEFAULT_COLOR));
	}

	public LoadingSpinner(double size, Color... colors)
	{
		this.size.set(size);
		setColors(colors);
		currentColor.set(getColors().getFirst());

		setMouseTransparent(true);
		setPickOnBounds(false);

		arc.setType(ArcType.OPEN);
		arc.setFill(Color.TRANSPARENT);
		arc.strokeProperty().bind(currentColor);
		arc.setStrokeLineCap(StrokeLineCap.ROUND);
		arc.strokeWidthProperty().bind(strokeWidth);

		arc.setManaged(false);

		arc.getTransforms().add(rotateTransform);

		getChildren().add(arc);

		this.size.addListener((_, _, _) -> updateGeometry());
		strokeWidth.addListener((_, _, _) -> updateGeometry());
		updateGeometry();

		rotator = new Timeline(
				new KeyFrame(Duration.ZERO,
						new KeyValue(rotateTransform.angleProperty(), 0, Interpolator.LINEAR)),
				new KeyFrame(LAP_DURATION,
						_ -> advanceToNextColor(),
						new KeyValue(rotateTransform.angleProperty(), 360, Interpolator.LINEAR))
		);
		rotator.setCycleCount(Animation.INDEFINITE);

		Interpolator ease = Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0);
		stretcher = new Timeline(
				new KeyFrame(Duration.ZERO,
						new KeyValue(arc.lengthProperty(), 20, ease),
						new KeyValue(arc.startAngleProperty(), 90, ease)),
				new KeyFrame(Duration.seconds(0.8),
						new KeyValue(arc.lengthProperty(), 270, ease),
						new KeyValue(arc.startAngleProperty(), -90, ease)),
				new KeyFrame(LAP_DURATION,
						new KeyValue(arc.lengthProperty(), 20, ease),
						new KeyValue(arc.startAngleProperty(), -270, ease))
		);
		stretcher.setCycleCount(Animation.INDEFINITE);

		sceneProperty().addListener((_, oldScene, newScene) -> {
			if(oldScene != null)
			{
				oldScene.windowProperty().removeListener(windowChangeListener);
			}
			if(newScene != null)
			{
				newScene.windowProperty().addListener(windowChangeListener);
				attachWindow(newScene.getWindow());
			}
			else
			{
				attachWindow(null);
			}
		});
	}

	/**
	 * Ties the running state of the animation to the showing state of the window instead of
	 * just the scene attachment. A node can stay attached to a {@link javafx.scene.Scene} that
	 * is no longer shown by any window (e.g. a stage that was hidden/closed but not disposed),
	 * in which case relying on {@code sceneProperty()} alone would leave the timelines running
	 * forever and racing the JavaFX pulse of whatever is rendered afterwards.
	 */
	private void attachWindow(Window newWindow)
	{
		if(window == newWindow)
		{
			return;
		}

		if(window != null)
		{
			window.showingProperty().removeListener(showingListener);
		}

		window = newWindow;

		if(window != null)
		{
			window.showingProperty().addListener(showingListener);
			if(window.isShowing())
			{
				play();
			}
			else
			{
				stop();
			}
		}
		else
		{
			stop();
		}
	}

	private void advanceToNextColor()
	{
		ObservableList<Color> list = colors.get();
		if(list == null || list.size() < 2)
		{
			return;
		}

		colorIndex = (colorIndex + 1) % list.size();
		Color from = currentColor.get();
		Color to = list.get(colorIndex);

		colorFade.stop();
		colorFade.getKeyFrames().setAll(
				new KeyFrame(Duration.ZERO, new KeyValue(currentColor, from)),
				new KeyFrame(COLOR_FADE_DURATION, new KeyValue(currentColor, to, Interpolator.EASE_BOTH))
		);
		colorFade.playFromStart();
	}

	private void updateGeometry()
	{
		double s = size.get();
		double sw = strokeWidth.get();
		double r = Math.max(0, (s - sw) / 2.0);

		arc.setCenterX(s / 2.0);
		arc.setCenterY(s / 2.0);
		arc.setRadiusX(r);
		arc.setRadiusY(r);

		rotateTransform.setPivotX(s / 2.0);
		rotateTransform.setPivotY(s / 2.0);

		setPrefSize(s, s);
		setMinSize(s, s);
		setMaxSize(s, s);
	}

	public void play()
	{
		rotator.play();
		stretcher.play();
	}

	public void stop()
	{
		rotator.stop();
		stretcher.stop();
		colorFade.stop();
	}

	public double getSize()
	{
		return size.get();
	}

	public void setSize(double value)
	{
		size.set(value);
	}

	public DoubleProperty sizeProperty()
	{
		return size;
	}

	public double getStrokeWidth()
	{
		return strokeWidth.get();
	}

	public void setStrokeWidth(double value)
	{
		strokeWidth.set(value);
	}

	public DoubleProperty strokeWidthProperty()
	{
		return strokeWidth;
	}

	public Color getCurrentColor()
	{
		return currentColor.get();
	}

	public ObjectProperty<Color> currentColorProperty()
	{
		return currentColor;
	}

	public ObservableList<Color> getColors()
	{
		return colors.get();
	}

	public ListProperty<Color> colorsProperty()
	{
		return colors;
	}

	public void setColors(Color... newColors)
	{
		if(newColors == null || newColors.length == 0)
		{
			newColors = new Color[]{Color.web(DEFAULT_COLOR)};
		}
		colors.set(FXCollections.observableArrayList(Arrays.asList(newColors)));
		colorIndex = 0;
		colorFade.stop();
		currentColor.set(colors.getFirst());
	}

	public void setColor(Color color)
	{
		setColors(color);
	}
}