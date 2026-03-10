package de.tobias.playwall.client.view;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaterialToastManager
{
	public enum ToastType
	{
		SUCCESS, WARNING, ERROR, INFO
	}

	private record ToastConfig(String accent, FontAwesomeType icon)
	{
	}

	private static final Map<ToastType, ToastConfig> CONFIGS = Map.of(
			ToastType.SUCCESS, new ToastConfig("#4CAF50", FontAwesomeType.CHECK_SOLID),
			ToastType.WARNING, new ToastConfig("#FF9800", FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID),
			ToastType.ERROR, new ToastConfig("#F44336", FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID),
			ToastType.INFO, new ToastConfig("#2196F3", FontAwesomeType.INFO_SOLID)
	);

	private final List<VBox> activeToasts = new ArrayList<>();
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
		ToastConfig config = CONFIGS.get(type);
		VBox toast = buildToast(title, message, config);

		// Unsichtbar hinzufügen damit JavaFX die Höhe berechnen kann
		toast.setOpacity(0);
		overlay.getChildren().add(toast);
		activeToasts.add(toast);

		// Warten bis Layout-Pass die tatsächliche Höhe kennt
		toast.layoutBoundsProperty().addListener(new ChangeListener<>()
		{
			@Override
			public void changed(ObservableValue<? extends Bounds> obs, Bounds oldVal, Bounds newVal)
			{
				if(newVal.getHeight() > 0)
				{
					toast.layoutBoundsProperty().removeListener(this);

					repositionToasts(false);

					FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
					fadeIn.setFromValue(0);
					fadeIn.setToValue(1);

					TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
					slideIn.setFromY(10);
					slideIn.setToY(0);
					slideIn.setInterpolator(Interpolator.EASE_OUT);

					new ParallelTransition(fadeIn, slideIn).play();
				}
			}
		});

		PauseTransition wait = new PauseTransition(Duration.seconds(4));
		wait.setOnFinished(e -> dismiss(toast));
		wait.play();
	}

	private VBox buildToast(String title, String message, ToastConfig config)
	{
		Region accent = new Region();
		accent.setPrefWidth(4);
		accent.setMinWidth(4);
		accent.setStyle("-fx-background-color: " + config.accent() + ";");

		FontIcon iconLabel = new FontIcon(config.icon());

		StackPane iconCircle = new StackPane(iconLabel);
		iconCircle.setMaxSize(28, 28);
		iconCircle.setPrefSize(28, 28);
		iconCircle.setMinSize(28, 28);
		// Icon-Kreis oben ausrichten bei mehrzeiligen Nachrichten
		StackPane.setAlignment(iconLabel, Pos.CENTER);
		iconCircle.setStyle(
				"-fx-background-color: " + config.accent() + ";" +
				"-fx-background-radius: 50%;"
		);

		Label titleLabel = new Label(title);
		titleLabel.setStyle("""
				    -fx-font-size: 13px;
				    -fx-font-weight: bold;
				    -fx-text-fill: #212121;
				""");

		Label messageLabel = new Label(message);
		messageLabel.setStyle("""
				    -fx-font-size: 12px;
				    -fx-text-fill: #757575;
				""");
		// Mehrzeilig erlauben, Breite begrenzen
		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(210);
		messageLabel.setMinHeight(Label.USE_PREF_SIZE); // <-- kein Abschneiden

		VBox textBox = new VBox(3, titleLabel, messageLabel);
		textBox.setAlignment(Pos.TOP_LEFT); // <-- oben ausrichten bei mehrzeilig
		VBox.setVgrow(messageLabel, Priority.ALWAYS);

		Label closeBtn = new Label("✕");
		closeBtn.setStyle("""
				    -fx-font-size: 11px;
				    -fx-text-fill: #BDBDBD;
				    -fx-cursor: hand;
				    -fx-padding: 4 6 4 6;
				""");

		HBox content = new HBox(12, iconCircle, textBox);
		content.setAlignment(Pos.TOP_LEFT); // <-- oben statt zentriert
		content.setPadding(new Insets(0, 0, 0, 12));
		HBox.setHgrow(textBox, Priority.ALWAYS);

		HBox row = new HBox(content, closeBtn);
		row.setAlignment(Pos.TOP_RIGHT); // <-- Close-Button oben rechts
		row.setPadding(new Insets(12, 8, 12, 0));
		HBox.setHgrow(content, Priority.ALWAYS);

		// Accent-Balken streckt sich automatisch mit der Höhe
		HBox outer = new HBox(accent, row);
		outer.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(row, Priority.ALWAYS);
		HBox.setHgrow(accent, Priority.NEVER);

		VBox toast = new VBox(outer);
		toast.setPrefWidth(TOAST_WIDTH);
		toast.setMaxWidth(TOAST_WIDTH);
		toast.setMaxHeight(VBox.USE_COMPUTED_SIZE); // <-- dynamische Höhe
		toast.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 4px;
				    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0, 0, 3);
				""");

		toast.setOnMouseEntered(e -> toast.setStyle("""
				    -fx-background-color: #FAFAFA;
				    -fx-background-radius: 4px;
				    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.26), 12, 0, 0, 5);
				"""));
		toast.setOnMouseExited(e -> toast.setStyle("""
				    -fx-background-color: white;
				    -fx-background-radius: 4px;
				    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 8, 0, 0, 3);
				"""));

		closeBtn.setOnMouseClicked(e -> dismiss(toast));

		return toast;
	}

	private void dismiss(VBox toast)
	{
		FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toast);
		fadeOut.setFromValue(1);
		fadeOut.setToValue(0);

		TranslateTransition slideOut = new TranslateTransition(Duration.millis(200), toast);
		slideOut.setToY(10);

		ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
		out.setOnFinished(e -> {
			overlay.getChildren().remove(toast);
			activeToasts.remove(toast);
			repositionToasts(true);
		});
		out.play();
	}

	private void repositionToasts(boolean animate)
	{
		double y = overlay.getHeight() - MARGIN;

		// Von unten nach oben stapeln, echte Höhe jedes Toasts verwenden
		for(int i = 0; i < activeToasts.size(); i++)
		{
			VBox t = activeToasts.get(i);
			double toastHeight = t.getLayoutBounds().getHeight();
			double x = overlay.getWidth() - TOAST_WIDTH - MARGIN;

			y -= toastHeight;

			if(animate)
			{
				TranslateTransition move = new TranslateTransition(Duration.millis(200), t);
				move.setToY(y - t.getLayoutY());
				move.setInterpolator(Interpolator.EASE_BOTH);
				move.play();
			}
			else
			{
				t.setLayoutX(x);
				t.setLayoutY(y);
			}

			y -= GAP;
		}
	}
}