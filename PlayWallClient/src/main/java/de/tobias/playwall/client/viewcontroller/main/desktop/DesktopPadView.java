package de.tobias.playwall.client.viewcontroller.main.desktop;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.tobias.playwall.client.PlayWallApiException;
import de.tobias.playwall.client.di.AppContextHolder;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.net.Client;
import de.tobias.playwall.client.view.pad.control.*;
import de.tobias.playwall.client.viewcontroller.main.PadView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static de.tobias.playwall.client.view.pad.control.PadStyleClasses.*;

public class DesktopPadView implements PadView
{
	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
	private Label playlistLabel;
	private Label errorLabel;

	private HBox infoBox;
	private Label timeLabel;

	private HBox preview;
	// private IPadContentView previewContent;

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

	private Pad pad;

	public DesktopPadView()
	{
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

		loopLabel = new PadLabel(new FontIcon(FontAwesomeType.REDO));
		triggerLabel = new PadLabel(new FontIcon(FontAwesomeType.LINK));
		playlistLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		errorLabel = new PadLabel(new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE));

		infoBox = new PadHBox(5);

		preview = PadHBox.deepStyled(STYLE_CLASS_PAD_TITLE, STYLE_CLASS_PAD_TITLE_INDEX);
		HBox.setHgrow(preview, Priority.ALWAYS);
		VBox.setVgrow(preview, Priority.ALWAYS);

		HBox.setHgrow(timeLabel, Priority.ALWAYS);
		timeLabel.setMaxWidth(Double.MAX_VALUE);
		timeLabel.setAlignment(Pos.CENTER_RIGHT);

		playBar = new PadProgressBar(0, STYLE_CLASS_PAD_PLAYBAR, STYLE_CLASS_PAD_PLAYBAR_INDEX);
		playBar.prefWidthProperty().bind(root.widthProperty());

		// Buttons
		// TODO: Event handler
		playButton = new PadButton(new FontIcon(FontAwesomeType.PLAY), null);
		pauseButton = new PadButton(new FontIcon(FontAwesomeType.PAUSE), null);
		nextButton = new PadButton(new FontIcon(FontAwesomeType.STEP_FORWARD), null);
		stopButton = new PadButton(new FontIcon(FontAwesomeType.STOP), null);
		newButton = new PadButton(new FontIcon(FontAwesomeType.FOLDER_OPEN), null);
		settingsButton = new PadButton(new FontIcon(FontAwesomeType.COG), null);

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE);
		notFoundLabel.getStyleClass().clear();
		notFoundLabel.setOpacity(0.75);
		notFoundLabel.setSize(80);
		notFoundLabel.setMouseTransparent(true);
		notFoundLabel.setVisible(false);

		// Button HBOX
		buttonBox = new PadHBox(STYLE_CLASS_PAD_BUTTON_BOX);

		buttonBox.getChildren().addAll(playButton);
		playButton.setOnAction(e -> {
			try
			{
				AppContextHolder.getInstance().get(Client.class).play(pad.getId());
			}
			catch(PlayWallApiException ex)
			{
				throw new RuntimeException(ex);
			}
		});

		root.getChildren().addAll(infoBox, preview, playBar, buttonBox);
		superRoot.getChildren().addAll(cueInContainer, root, notFoundLabel);
	}

	@Override
	public Node getRootNode()
	{
		return superRoot;
	}

	@Override
	public void setContentView(Pad pad)
	{
		this.pad = pad;

		preview.getChildren().clear();
		if(pad != null)
		{
			preview.getChildren().add(new Text(pad.getName()));
		}
	}
}
