package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.*;

public class MaterialToastManager
{
	public enum ToastType
	{
		SUCCESS, WARNING, ERROR, INFO
	}

	private static final Map<ToastType, FontAwesomeType> ICONS = Map.of(
			ToastType.SUCCESS, FontAwesomeType.CHECK_SOLID,
			ToastType.WARNING, FontAwesomeType.CIRCLE_EXCLAMATION_SOLID,
			ToastType.ERROR, FontAwesomeType.CIRCLE_XMARK_SOLID,
			ToastType.INFO, FontAwesomeType.INFO_SOLID
	);

	private final List<Node> activeToasts = new ArrayList<>();
	private final Set<Node> dismissingToasts = new HashSet<>();
	private final Pane overlay;

	private static final double TOAST_WIDTH = 320;
	private static final double GAP = 8;
	private static final double MARGIN = 16;

	public MaterialToastManager(StackPane parent)
	{
		this.overlay = new Pane();
		this.overlay.setPickOnBounds(false);

		parent.getChildren().add(overlay);

		overlay.prefWidthProperty().bind(parent.widthProperty());
		overlay.prefHeightProperty().bind(parent.heightProperty());
	}

	public void show(String title, String message, ToastType type)
	{
		final Node toast = buildToast(title, message, type);

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

					final FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
					fadeIn.setFromValue(0);
					fadeIn.setToValue(1);

					final TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
					slideIn.setFromY(10);
					slideIn.setToY(0);
					slideIn.setInterpolator(Interpolator.EASE_OUT);

					new ParallelTransition(fadeIn, slideIn).play();
				}
			}
		});

		final PauseTransition wait = new PauseTransition(Duration.seconds(4));
		wait.setOnFinished(_ -> dismiss(toast));
		wait.play();
	}

	private Node buildToast(String title, String message, ToastType type)
	{
		final Region accent = new Region();
		accent.setPrefWidth(4);
		accent.setMinWidth(4);
		accent.getStyleClass().addAll("accent", type.name().toLowerCase());

		final FontIcon iconLabel = new FontIcon(ICONS.get(type));

		final StackPane iconCircle = new StackPane(iconLabel);
		iconCircle.setMaxSize(28, 28);
		iconCircle.setPrefSize(28, 28);
		iconCircle.setMinSize(28, 28);
		iconCircle.getStyleClass().addAll("icon-circle", type.name().toLowerCase());
		StackPane.setAlignment(iconLabel, Pos.CENTER);

		final Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("title");

		final Label messageLabel = new Label(message);
		messageLabel.getStyleClass().add("message");
		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(210);
		messageLabel.setMinHeight(Region.USE_PREF_SIZE);

		final FontIcon closeIcon = new FontIcon(FontAwesomeType.XMARK_SOLID);
		closeIcon.getStyleClass().add("close-button");

		final VBox textBox = new VBox(3, titleLabel, messageLabel);
		textBox.setAlignment(Pos.TOP_LEFT);
		VBox.setVgrow(messageLabel, Priority.ALWAYS);

		final HBox content = new HBox(12, iconCircle, textBox);
		content.setAlignment(Pos.TOP_LEFT);
		content.setPadding(new Insets(0, 0, 0, 12));
		HBox.setHgrow(textBox, Priority.ALWAYS);

		final HBox row = new HBox(content, closeIcon);
		row.setAlignment(Pos.TOP_RIGHT);
		row.setPadding(new Insets(12, 8, 12, 0));
		HBox.setHgrow(content, Priority.ALWAYS);

		final HBox outer = new HBox(accent, row);
		outer.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(row, Priority.ALWAYS);
		HBox.setHgrow(accent, Priority.NEVER);

		final VBox toast = new VBox(outer);
		toast.getStyleClass().add("toast");
		toast.setPrefWidth(TOAST_WIDTH);
		toast.setMaxWidth(TOAST_WIDTH);
		toast.setMaxHeight(Region.USE_COMPUTED_SIZE);

		closeIcon.setOnMouseClicked(_ -> dismiss(toast));

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

		final FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
		fadeOut.setFromValue(toast.getOpacity());
		fadeOut.setToValue(0);

		final ParallelTransition out = new ParallelTransition(fadeOut);
		out.setOnFinished(_ -> {
			overlay.getChildren().remove(toast);
			dismissingToasts.remove(toast);
		});
		out.play();
	}

	private void repositionToasts(boolean animate)
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

				t.setLayoutY(y);
				t.setTranslateY(-delta);

				final TranslateTransition move = new TranslateTransition(Duration.millis(200), t);
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
}