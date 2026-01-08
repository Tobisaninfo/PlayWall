package de.tobias.playwall.client.viewcontroller.main.desktop;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.*;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.service.ClientPadController;
import de.tobias.playwall.client.utils.NodeWalker;
import de.tobias.playwall.client.view.pad.PadIndexable;
import de.tobias.playwall.client.view.pad.control.*;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.client.viewcontroller.main.PadView;
import de.tobias.playwall.client.viewcontroller.settings.pad.PadSettingsViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Getter;

import static de.tobias.playwall.client.view.pad.control.PadStyleClasses.*;

@Getter
public class DesktopPadView implements PadView
{
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

	private PadStatus previousStatus;
	private PadStatus status;
	private PadSettingsViewController padSettingsViewController;

	private Duration padDuration;

	@Getter
	private Pad pad;

	public DesktopPadView()
	{
		fluentClient = AppContextHolder.getInstance().get(FluentClient.class);
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
		this.pad = controller.getPad();
		if(pad != null)
		{
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

			setPadDuration(controller.getDuration());
		}
		else
		{
			removeStyleClasses();
		}
		busyView.showProgress(false);
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
			Platform.runLater(this::updateButtonStates);
		}
	}

	@Override
	public void updatePlayPosition(Duration position)
	{
		if(padDuration != null)
		{
			playBar.setProgress(position.toMillis() / padDuration.toMillis());
			Logger.debug("Update PlayPosition: " + position);
		}
	}

	@Override
	public void setPadDuration(Duration padDuration)
	{
		this.padDuration = padDuration;
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
			// TODO: error handling
			updateStatus(this.previousStatus);
			throw new RuntimeException(ex);
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
			// TODO: error handling
			updateStatus(this.previousStatus);
			throw new RuntimeException(ex);
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
			// TODO: error handling
			updateStatus(this.previousStatus);
			throw new RuntimeException(ex);
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
				// TODO: error handling
				updateStatus(this.previousStatus);
				throw new RuntimeException(ex);
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

		padSettingsViewController.showAndWait(new PadSettingsViewController.Param(pad), superRoot.getScene().getWindow());
	}
}
