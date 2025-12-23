package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

		final FilteredList<IconEntry> filteredData = new FilteredList<>(getData(), s -> true);

		listView.setItems(filteredData);

		textFieldSearch.textProperty().addListener((_, oldValue, newValue) -> {
			if(newValue == null || newValue.isEmpty())
			{
				filteredData.setPredicate(s -> true);
			}
			else
			{
				final String newValueLowerCase = newValue.toLowerCase();
				filteredData.setPredicate(s -> s.description().toLowerCase().contains(newValueLowerCase) || s.fontIconType().toString().toLowerCase().contains(newValueLowerCase));
			}
		});
	}

	private ObservableList<IconEntry> getData()
	{
		final ObservableList<IconEntry> data = FXCollections.observableArrayList();
		data.add(new IconEntry(FontAwesomeType.FLOPPY_DISK_SOLID, "Save"));
		return data;
	}
}
