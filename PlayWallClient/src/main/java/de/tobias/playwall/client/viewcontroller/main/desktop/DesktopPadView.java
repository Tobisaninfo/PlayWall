package de.tobias.playwall.client.viewcontroller.main.desktop;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.view.pad.control.*;
import de.tobias.playwall.client.viewcontroller.FileChooserWrapper;
import de.tobias.playwall.client.viewcontroller.main.PadView;
import de.tobias.playwall.common.utils.FileFormats;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Optional;

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
	private Button nextButton;
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
		VBox cueInContainer = new VBox(cueInLayer);

		indexLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		timeLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);

		loopLabel = new PadLabel(new FontIcon(FontAwesomeType.ARROW_ROTATE_LEFT_SOLID));
		triggerLabel = new PadLabel(new FontIcon(FontAwesomeType.LINK_SOLID));
		playlistLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		errorLabel = new PadLabel(new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID));

		infoBox = new PadHBox(5);

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
		nextButton = new PadButton(new FontIcon(FontAwesomeType.FORWARD_SOLID), null);
		stopButton = new PadButton(new FontIcon(FontAwesomeType.STOP_SOLID), this::onStopAction);
		newButton = new PadButton(new FontIcon(FontAwesomeType.FOLDER_OPEN_SOLID), this::onNewAction);
		settingsButton = new PadButton(new FontIcon(FontAwesomeType.GEAR_SOLID), null);

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID);
		notFoundLabel.getStyleClass().clear();
		notFoundLabel.setOpacity(0.75);
		notFoundLabel.setSize(80);
		notFoundLabel.setMouseTransparent(true);
		notFoundLabel.setVisible(false);

		// Button HBOX
		buttonBox = new PadHBox(STYLE_CLASS_PAD_BUTTON_BOX);

		buttonBox.getChildren().addAll(playButton, pauseButton, stopButton, newButton);

		root.getChildren().addAll(infoBox, previewBox, playBar, buttonBox);
		superRoot.getChildren().addAll(cueInContainer, root, notFoundLabel);
	}

	@Override
	public Node getRootNode()
	{
		return superRoot;
	}

	@Override
	public void updateFromPad(Pad pad)
	{
		this.pad = pad;
		if(pad != null)
		{
			padBuilder = fluentClient.pad(pad.getId());
			namePreviewLabel.setText(pad.getName());
		}
		busyView.showProgress(false);
	}

	@Override
	public void showLoading(boolean isLoading)
	{
		busyView.showProgress(isLoading);
	}

	private void onPlayAction(ActionEvent event)
	{
		try
		{
			padBuilder.play();
		}
		catch(PlayWallApiException ex)
		{
			// TODO: error handling
			throw new RuntimeException(ex);
		}
	}

	private void onPauseAction(ActionEvent event)
	{
		try
		{
			padBuilder.pause();
		}
		catch(PlayWallApiException ex)
		{
			// TODO: error handling
			throw new RuntimeException(ex);
		}
	}

	private void onStopAction(ActionEvent event)
	{
		try
		{
			padBuilder.stop();
		}
		catch(PlayWallApiException ex)
		{
			// TODO: error handling
			throw new RuntimeException(ex);
		}
	}

	private void onNewAction(ActionEvent event)
	{
		final Window owner = ((Node) event.getTarget()).getScene().getWindow();
		final FileChooserWrapper fileChooser = AppContextHolder.getInstance().get(FileChooserWrapper.class);
		fileChooser.setExtensionFilter(FileFormats.FILE_FORMATS.stream().map(format ->
				new FileChooser.ExtensionFilter(
						Localization.getString("FileFormat." + format.contentType().name()),
						format.extensions().stream().map(ext -> "*." + ext).toList()
				)).toList());
		final Optional<Path> path = fileChooser.showOpenFile(owner);

		if(path.isPresent())
		{
			try
			{
				padBuilder.newMedia(path.get());
			}
			catch(PlayWallApiException ex)
			{
				// TODO: error handling
				throw new RuntimeException(ex);
			}
		}
	}
}
