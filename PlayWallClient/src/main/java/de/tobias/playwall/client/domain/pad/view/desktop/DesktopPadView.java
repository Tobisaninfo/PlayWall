package de.tobias.playwall.client.domain.pad.view.desktop;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.domain.pad.*;
import de.tobias.playwall.client.domain.pad.view.PadIndexable;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.pad.view.control.*;
import de.tobias.playwall.client.domain.pad.view.settings.PadSettingsViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.utils.NodeWalker;
import de.tobias.playwall.client.utils.PadTimeUtils;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.components.ErrorAlert;
import de.tobias.playwall.common.api.StackTraceError;
import de.tobias.playwall.common.api.common.TimeMode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;
import org.controlsfx.dialog.ExceptionDialog;

import static de.tobias.playwall.client.domain.pad.view.control.PadStyleClasses.*;
import static de.tobias.playwall.client.view.components.PseudoClasses.PLAY_CLASS;

@Getter
public class DesktopPadView implements PadView
{
	private Tooltip tooltip;

	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
	private Label playlistLabel;
	private Label errorLabel;

	private HBox infoBox;
	private Label timeLabel;

	private HBox previewBox;
	private Label namePreviewLabel;

	private FontIcon notFoundLabel;

	private ProgressBar playBar;
	private Button playButton;
	private Button pauseButton;
	private Button stopButton;
	private Button newButton;
	private Button settingsButton;
	private HBox buttonBox;

	private StackPane superRoot;
	private VBox root;
	private BusyView busyView;

	private Label cueInLayer;

	private final FluentClient fluentClient;
	private FluentClient.PadBuilder padBuilder;

	private final PadTimeUtils padTimeUtils;

	private PadStatus previousStatus;
	private PadStatus status;
	private PadSettingsViewController padSettingsViewController;

	@Getter
	private ClientPadController padController;

	public DesktopPadView()
	{
		fluentClient = AppContextHolder.getInstance().get(FluentClient.class);
		padTimeUtils = AppContextHolder.getInstance().get(PadTimeUtils.class);
		setupView();
	}

	private void setupView()
	{
		superRoot = new PadStackPane(STYLE_CLASS_PAD, STYLE_CLASS_PAD_INDEX);
		root = new PadVBox(STYLE_CLASS_PAD_BUTTON_ROOT);
		busyView = new BusyView(superRoot);

		cueInLayer = PadLabel.empty(STYLE_CLASS_PAD_CUE_IN, STYLE_CLASS_PAD_CUE_IN_INDEX);
		cueInLayer.prefHeightProperty().bind(root.heightProperty());
		final VBox cueInContainer = new VBox(cueInLayer);

		indexLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		timeLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);

		loopLabel = new PadLabel(new FontIcon(FontAwesomeType.ARROW_ROTATE_LEFT_SOLID));
		triggerLabel = new PadLabel(new FontIcon(FontAwesomeType.LINK_SOLID));
		playlistLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		errorLabel = new PadLabel(new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID));

		infoBox = new PadHBox(5);
		infoBox.getChildren().setAll(indexLabel, loopLabel, triggerLabel, playlistLabel, errorLabel, timeLabel);

		previewBox = PadHBox.deepStyled(STYLE_CLASS_PAD_TITLE, STYLE_CLASS_PAD_TITLE_INDEX);
		HBox.setHgrow(previewBox, Priority.ALWAYS);
		VBox.setVgrow(previewBox, Priority.ALWAYS);

		namePreviewLabel = new Label();
		namePreviewLabel.setWrapText(true);
		namePreviewLabel.setAlignment(Pos.CENTER);
		namePreviewLabel.setTextAlignment(TextAlignment.CENTER);
		namePreviewLabel.setMaxHeight(Double.MAX_VALUE);
		namePreviewLabel.prefWidthProperty().bind(previewBox.widthProperty());
		VBox.setVgrow(namePreviewLabel, Priority.ALWAYS);

		previewBox.getChildren().add(namePreviewLabel);

		HBox.setHgrow(timeLabel, Priority.ALWAYS);
		timeLabel.setMaxWidth(Double.MAX_VALUE);
		timeLabel.setAlignment(Pos.CENTER_RIGHT);

		playBar = new PadProgressBar(0, STYLE_CLASS_PAD_PLAYBAR, STYLE_CLASS_PAD_PLAYBAR_INDEX);
		playBar.prefWidthProperty().bind(root.widthProperty());

		// Buttons
		playButton = new PadButton(new FontIcon(FontAwesomeType.PLAY_SOLID), this::onPlayAction);
		pauseButton = new PadButton(new FontIcon(FontAwesomeType.PAUSE_SOLID), this::onPauseAction);
		stopButton = new PadButton(new FontIcon(FontAwesomeType.STOP_SOLID), this::onStopAction);
		newButton = new PadButton(new FontIcon(FontAwesomeType.FOLDER_OPEN_SOLID), this::onNewAction);
		settingsButton = new PadButton(new FontIcon(FontAwesomeType.GEAR_SOLID), this::onSettingsAction);

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
		notFoundLabel.getStyleClass().clear();
		notFoundLabel.setOpacity(0.75);
		notFoundLabel.setSize(80);
		notFoundLabel.setMouseTransparent(true);
		notFoundLabel.setVisible(false);

		// Button HBOX
		buttonBox = new PadHBox(STYLE_CLASS_PAD_BUTTON_BOX);
		buttonBox.getChildren().addAll(playButton, pauseButton, stopButton, newButton, settingsButton);
		// Make all buttons the same width
		buttonBox.prefWidthProperty().bind(superRoot.widthProperty());
		for(Node child : buttonBox.getChildren())
		{
			if(child instanceof Region region)
			{
				HBox.setHgrow(region, Priority.ALWAYS);
				region.setMaxWidth(Double.MAX_VALUE);
			}
		}

		// alle Labels in der InfoBox sollen die gleiche Höhe haben, damit die Icons auf gleicher höhe sind
		for(Node child : infoBox.getChildren())
		{
			if(child instanceof Label label)
			{
				label.setMaxHeight(Double.MAX_VALUE);
			}
		}

		root.getChildren().addAll(infoBox, previewBox, playBar, buttonBox);
		superRoot.getChildren().addAll(cueInContainer, root, notFoundLabel);

		updateStatus(PadStatus.EMPTY);
	}

	@Override
	public Node getRootNode()
	{
		return superRoot;
	}

	@Override
	public void updateFromPad(int currentPage, ClientPadController controller)
	{
		this.padController = controller;
		updateTooltip();

		if(padController == null)
		{
			reset();
			return;
		}
		final Pad pad = padController.getPad();
		padBuilder = fluentClient.pad(pad.getId());
		namePreviewLabel.setText(pad.getName());

		indexLabel.setText(pad.getReadablePosition());

		loopLabel.setVisible(false);
		triggerLabel.setVisible(false);
		playlistLabel.setVisible(false);
		notFoundLabel.setVisible(false);
		errorLabel.setVisible(false);

		final PadContent padContent = pad.getContent();
		if(padContent instanceof Loopable loopable)
		{
			loopLabel.setVisible(loopable.isLoop());
		}

		if(controller.getStatus() != null)
		{
			updateStatus(controller.getStatus());
		}
		else if(padContent != null)
		{
			updateStatus(PadStatus.READY);
		}
		else
		{
			updateStatus(PadStatus.EMPTY);
		}
		addStyleClasses(new PadIndex(pad.getPosition(), currentPage));

		updateTimeNodes();
		busyView.showProgress(false);
	}

	private void reset()
	{
		namePreviewLabel.setText(null);
		timeLabel.setText(null);

		loopLabel.setVisible(false);
		triggerLabel.setVisible(false);
		playlistLabel.setVisible(false);
		notFoundLabel.setVisible(false);
		errorLabel.setVisible(false);

		removeStyleClasses();

		busyView.showProgress(false);
	}

	private void updateTooltip()
	{
		if(tooltip != null)
		{
			Tooltip.uninstall(superRoot, tooltip);
			tooltip = null;
		}
		if(padController != null && AppContextHolder.getInstance().get(App.class).isDebug())
		{
			tooltip = new Tooltip(padController.getPad().getId().toString());
			Tooltip.install(superRoot, tooltip);
		}
	}

	@Override
	public void showLoading(boolean isLoading)
	{
		busyView.showProgress(isLoading);
		updateStatus(isLoading ? PadStatus.EMPTY : PadStatus.READY);
	}

	@Override
	public void updateStatus(PadStatus status)
	{
		this.previousStatus = this.status;
		this.status = status;

		if(this.previousStatus != status)
		{
			Platform.runLater(() -> {
				this.updateButtonStates();
				this.updateTimeNodes();

				NodeWalker.getAllNodes(superRoot)
						.forEach(node -> node.pseudoClassStateChanged(PLAY_CLASS, status == PadStatus.PLAY));
			});
		}
	}

	@Override
	public void updateTimeNodes()
	{
		if(padController == null)
		{
			this.timeLabel.setText(null);
			this.playBar.setProgress(0.0);

			return;
		}

		final Duration position = padController.getPosition();
		final Duration duration = padController.getDuration();

		if(duration == null)
		{
			this.timeLabel.setText(null);
			this.playBar.setProgress(0.0);

			return;
		}

		if((status == PadStatus.PLAY || status == PadStatus.PAUSE) && position != null)
		{
			updateTimeLabelByTimeMode(duration, position);

			this.playBar.setProgress(padController.getPosition().toMillis() / duration.toMillis());
		}
		else
		{
			this.timeLabel.setText(padTimeUtils.formatDurationToString(duration));
			this.playBar.setProgress(0.0);
		}
	}

	@Override
	public void disableSettingsButton(boolean disabled)
	{
		settingsButton.setDisable(disabled);
	}

	private void updateTimeLabelByTimeMode(Duration duration, Duration position)
	{
		final TimeMode padTimeMode = padController.getPad().getTimeMode();

		if(padTimeMode == null)
		{
			this.timeLabel.setText(padTimeUtils.formatTimeMode(padController.getProjectMetadata().getTimeMode(), duration, position));
		}
		else
		{
			this.timeLabel.setText(padTimeUtils.formatTimeMode(padTimeMode, duration, position));
		}
	}

	public void addStyleClasses(PadIndex index)
	{
		NodeWalker.getAllNodes((Parent) getRootNode())
				.stream()
				.filter(PadIndexable.class::isInstance)
				.forEach(node -> ((PadIndexable) node).setIndex(index));
	}

	public void removeStyleClasses()
	{
		NodeWalker.getAllNodes((Parent) getRootNode())
				.stream()
				.filter(PadIndexable.class::isInstance)
				.forEach(node -> ((PadIndexable) node).setIndex(null));
	}

	private void onPlayAction(ActionEvent event)
	{
		try
		{
			padBuilder.play();
			updateStatus(PadStatus.PLAY);
		}
		catch(PlayWallApiException ex)
		{
			updateStatus(this.previousStatus);
			Logger.error(ex);
			ErrorAlert.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_PLAY), ex.getMessage(), ex.getError(), superRoot.getScene().getWindow()).showAndWait();
		}
	}

	private void onPauseAction(ActionEvent event)
	{
		try
		{
			padBuilder.pause();
			updateStatus(PadStatus.PAUSE);
		}
		catch(PlayWallApiException ex)
		{
			updateStatus(this.previousStatus);
			Logger.error(ex);
			ErrorAlert.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_PAUSE), ex.getMessage(), ex.getError(), superRoot.getScene().getWindow()).showAndWait();
		}
	}

	private void onStopAction(ActionEvent event)
	{
		try
		{
			padBuilder.stop();
			updateStatus(PadStatus.READY);
		}
		catch(PlayWallApiException ex)
		{
			updateStatus(this.previousStatus);
			Logger.error(ex);
			ErrorAlert.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_STOP), ex.getMessage(), ex.getError(), superRoot.getScene().getWindow()).showAndWait();
		}
	}

	private void onNewAction(ActionEvent event)
	{
		final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
		fileChooser.showByActionEvent(event).ifPresent(path -> {
			try
			{
				padBuilder.newMedia(path);
				updateStatus(PadStatus.READY);
			}
			catch(PlayWallApiException ex)
			{
				updateStatus(this.previousStatus);
				Logger.error(ex);
				ErrorAlert.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_PAD_LOAD), ex.getMessage(), ex.getError(), superRoot.getScene().getWindow()).showAndWait();
			}
		});
	}

	private void updateButtonStates()
	{
		switch(status)
		{
			case EMPTY ->
			{
				buttonBox.getChildren().setAll(newButton, settingsButton);
				stopButton.setDisable(true);
			}
			case READY ->
			{
				buttonBox.getChildren().setAll(playButton, stopButton, settingsButton);
				stopButton.setDisable(true);
			}
			case PLAY ->
			{
				buttonBox.getChildren().setAll(pauseButton, stopButton, settingsButton);
				stopButton.setDisable(false);
			}
			case PAUSE ->
			{
				buttonBox.getChildren().setAll(playButton, stopButton, settingsButton);
				stopButton.setDisable(false);
			}
		}
	}

	private void onSettingsAction(ActionEvent event)
	{
		if(padSettingsViewController == null)
		{
			padSettingsViewController = AppContextHolder.getInstance().get(PadSettingsViewController.class);
		}

		padSettingsViewController.showAndWait(new PadSettingsViewController.Param(padController.getPad()), superRoot.getScene().getWindow());
	}
}
