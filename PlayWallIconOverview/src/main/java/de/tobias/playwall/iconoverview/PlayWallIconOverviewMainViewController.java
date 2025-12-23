package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;

import static de.thecodelabs.utils.util.Localization.getString;

@Getter(AccessLevel.PACKAGE)
public class PlayWallIconOverviewMainViewController extends NVC
{
	@FXML
	private Label labelTitle;

	@FXML
	private TextField textFieldSearch;

	@FXML
	private ListView<IconEntry> listView;

	private final App app;

	public PlayWallIconOverviewMainViewController(Stage stage, App app)
	{
		this.app = app;

		load("de/tobias/playwall/iconoverview/view", "MainView.fxml", Localization.getBundle());
		final NVCStage nvcStage = applyViewControllerToStage(stage);
		nvcStage.setImage(PlayWallIconOverviewMain.icon);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		stage.setTitle(app.getInfo().getName());
		stage.setMinWidth(400);
		stage.setMinHeight(400);
		stage.setWidth(800);
		stage.setHeight(600);
		stage.setResizable(true);
		stage.centerOnScreen();

		labelTitle.setText(app.getInfo().getName());

		listView.setPlaceholder(new Label(getString("list.placeholder")));
		listView.setCellFactory(_ -> new IconCell());

		listView.getItems().addAll(new IconEntry(FontAwesomeType.FLOPPY_DISK_SOLID, "Save"));
	}
}
