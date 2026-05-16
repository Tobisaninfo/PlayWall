package de.tobias.playwall.client.domain.project.view.media;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.view.ViewControllerBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

@ViewController(path = "de/tobias/playwall/client/view/media", view = "ReplaceMediaView")
public class ReplaceMediaViewController extends ViewControllerBase
{
	@FXML
	private TableView<MissingMediaEntry> table;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnPageName;
	@FXML
	private TableColumn<MissingMediaEntry, Number> columnPadPosition;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnPadName;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnOldPath;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnSolution;
	@FXML
	private TableColumn<MissingMediaEntry, MissingMediaEntry> columnActions;

	private final ObservableList<MissingMediaEntry> entries = FXCollections.observableArrayList();

	@Override
	public void init()
	{
		columnPageName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPageName()));
		columnPadPosition.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPadPosition()));
		columnPadName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPadName()));
		columnOldPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOldMediaPath()));
		columnSolution.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMissingMediaSolutionType().name()));
		columnActions.setCellFactory(_ -> new MissingMediaEntryTableCell());

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setItems(entries);
		table.setPlaceholder(new Label(Localization.getString(Strings.UI_REPLACE_MEDIA_PLACEHOLDER)));
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stage.setTitle(Localization.getString(Strings.UI_MENU_EDIT_REPLACE_MEDIA));
		stage.setWidth(1200);
		stage.setMinWidth(1200);
		stage.setHeight(700);
		stage.setMinHeight(700);
	}

	public void setEntries(List<MissingMediaEntry> entries)
	{
		this.entries.setAll(entries);
	}
}
