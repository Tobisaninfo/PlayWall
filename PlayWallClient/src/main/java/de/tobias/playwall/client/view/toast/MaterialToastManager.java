package de.tobias.playwall.client.view.toast;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialToastManager
{
	private final List<Node> activeToasts = new ArrayList<>();
	private final Set<Node> dismissingToasts = new HashSet<>();
	private final Pane overlay;

	static final double TOAST_WIDTH = 320;
	private static final double GAP = 8;
	private static final double MARGIN = 16;

	private static final Duration DURATION_FADE_IN = Duration.millis(250);
	private static final Duration DURATION_PAUSE = Duration.seconds(4);
	private static final Duration DURATION_FADE_OUT = Duration.millis(250);

	public MaterialToastManager(StackPane parent)
	{
		this.overlay = new Pane();
		this.overlay.getStyleClass().add("toast-overlay");
		this.overlay.setPickOnBounds(false);

		parent.getChildren().add(overlay);

		overlay.prefWidthProperty().bind(parent.widthProperty());
		overlay.prefHeightProperty().bind(parent.heightProperty());

		overlay.widthProperty().addListener((_, _, _) -> repositionToasts(false));
		overlay.heightProperty().addListener((_, _, _) -> repositionToasts(false));
	}

	public void show(String title, String message, ToastType type, ToastAction... actions)
	{
		final Node toast = buildAndScheduleToast(title, message, type, List.of(actions));

		final PauseTransition wait = new PauseTransition(DURATION_PAUSE);
		wait.setOnFinished(_ -> dismiss(toast));
		wait.play();
	}

	public Toast showPermanent(String title, String message, ToastType type, ToastAction... actions)
	{
		return buildAndScheduleToast(title, message, type, List.of(actions));
	}

	private Toast buildAndScheduleToast(String title, String message, ToastType type, List<ToastAction> actions)
	{
		final Toast toast = new Toast(title, message, type, actions, this::dismiss, this);

		toast.setOpacity(0);
		overlay.getChildren().add(toast);
		activeToasts.add(toast);

		toast.layoutBoundsProperty().addListener(new ChangeListener<>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> obs, Bounds oldVal, Bounds newVal)
			{
				if(newVal.getHeight() > 0)
				{
					toast.layoutBoundsProperty().removeListener(this);

					repositionToasts(false);

					final FadeTransition fadeIn = new FadeTransition(DURATION_FADE_IN, toast);
					fadeIn.setFromValue(0);
					fadeIn.setToValue(1);

					final TranslateTransition slideIn = new TranslateTransition(DURATION_FADE_IN, toast);
					slideIn.setFromY(10);
					slideIn.setToY(0);
					slideIn.setInterpolator(Interpolator.EASE_OUT);

					new ParallelTransition(fadeIn, slideIn).play();
				}
			}
		});

		return toast;
	}

	private void dismiss(Node toast)
	{
		if(!dismissingToasts.add(toast))
		{
			return;
		}

		activeToasts.remove(toast);
		repositionToasts(true);

		final FadeTransition fadeOut = new FadeTransition(DURATION_FADE_OUT, toast);
		fadeOut.setFromValue(toast.getOpacity());
		fadeOut.setToValue(0);

		final ParallelTransition out = new ParallelTransition(fadeOut);
		out.setOnFinished(_ -> {
			overlay.getChildren().remove(toast);
			dismissingToasts.remove(toast);
		});
		out.play();
	}

	void repositionToasts(boolean animate)
	{
		double y = overlay.getHeight() - MARGIN;

		for(Node t : activeToasts)
		{
			final double toastHeight = t.getLayoutBounds().getHeight();
			final double x = overlay.getWidth() - TOAST_WIDTH - MARGIN;

			y -= toastHeight;

			if(animate)
			{
				final double currentAbsY = t.getLayoutY() + t.getTranslateY();
				final double delta = y - currentAbsY;

				t.setLayoutX(x);
				t.setLayoutY(y);
				t.setTranslateY(-delta);

				final TranslateTransition move = new TranslateTransition(DURATION_FADE_OUT, t);
				move.setFromY(-delta);
				move.setToY(0);
				move.setInterpolator(Interpolator.EASE_BOTH);
				move.play();
			}
			else
			{
				t.setLayoutX(x);
				t.setLayoutY(y);
				t.setTranslateY(0);
			}

			y -= GAP;
		}
	}

	public boolean isToastVisible(Toast toast)
	{
		return activeToasts.contains(toast);
	}
}