package de.tobias.playwall.client.domain.project.view.media;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.net.PlayWallApiException;
import de.tobias.playwall.client.view.FileChooserWrapper;
import de.tobias.playwall.client.view.ViewControllerBase;
import de.tobias.playwall.client.view.components.ErrorAlertBuilder;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ViewController(path = "de/tobias/playwall/client/view/media", view = "ReplaceMediaView")
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
public class ReplaceMediaViewController extends ViewControllerBase
{
	@FXML
	private TableView<MissingMediaEntry> table;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnPageName;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnPadPosition;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnPadName;
	@FXML
	private TableColumn<MissingMediaEntry, String> columnOldPath;
	@FXML
	private TableColumn<MissingMediaEntry, MissingMediaEntry> columnSolution;
	@FXML
	private TableColumn<MissingMediaEntry, MissingMediaEntry> columnActions;

	private final ObservableList<MissingMediaEntry> entries = FXCollections.observableArrayList();

	private final FluentClient client;

	private final ErrorAlertBuilder errorAlertBuilder;

	private final FileChooserWrapper fileChooserWrapper;

	@Override
	public void init()
	{
		columnPageName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPageName()));
		columnPadPosition.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPadPosition()));
		columnPadName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPadName()));
		columnOldPath.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOldMediaPath()));

		columnSolution.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
		columnSolution.setCellFactory(_ -> new MissingMediaEntrySolutionCell());

		columnActions.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
		columnActions.setCellFactory(_ -> new MissingMediaEntryActionCell());

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setItems(entries);
		table.setPlaceholder(new Label(Localization.getString(Strings.UI_REPLACE_MEDIA_PLACEHOLDER)));

		table.getSortOrder().add(columnPageName);
		table.getSortOrder().add(columnPadPosition);
		columnPadPosition.setComparator(Comparator.comparingInt(Integer::parseInt));

		Platform.runLater(() -> table.sort());
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		super.initStage(stageContainer, stage);
		stage.setTitle(Localization.getString(Strings.UI_MENU_EDIT_REPLACE_MEDIA));
		stage.setWidth(1200);
		stage.setMinWidth(1200);
		stage.setHeight(700);
		stage.setMinHeight(200);
	}

	public void setEntries(List<MissingMediaEntry> entries)
	{
		this.entries.setAll(entries);
	}

	@FXML
	protected void saveButtonHandler(ActionEvent event)
	{
		final Map<UUID, String> newMediaPathsByPadId = entries.stream()
				.filter(e -> e.getMissingMediaSolutionType() == MissingMediaSolutionType.REPLACE)
				.collect(Collectors.toMap(MissingMediaEntry::getPadId, MissingMediaEntry::getNewMediaPath));

		final Set<UUID> padIdsToDelete = entries.stream()
				.filter(e -> e.getMissingMediaSolutionType() == MissingMediaSolutionType.DELETE)
				.map(MissingMediaEntry::getPadId)
				.collect(Collectors.toSet());

		if(newMediaPathsByPadId.isEmpty() && padIdsToDelete.isEmpty())
		{
			return;
		}

		getStageContainer().ifPresent(NVCStage::close);

		try
		{
			client.currentProject().batchReplaceMedia(newMediaPathsByPadId, padIdsToDelete);
		}
		catch(PlayWallApiException e)
		{
			log.error("Cannot update pad media paths", e);
			errorAlertBuilder.createErrorAlert(null, Localization.getString(Strings.UI_ERRORS_REPLACE_MEDIA), e.getMessage(), e.getError(), getContainingWindow()).showAndWait();
		}
	}

	@FXML
	protected void cancelButtonHandler(ActionEvent event)
	{
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	protected void autoSearchButtonHandler(ActionEvent event)
	{
		fileChooserWrapper.showOpenFolder(getContainingWindow()).ifPresent(this::applyAutoSearch);
	}

	void applyAutoSearch(Path folder)
	{
		matchMediaFiles(folder, entries);
		table.refresh();
	}

	void matchMediaFiles(Path folder, List<MissingMediaEntry> entries)
	{
		Map<String, Path> filesByFileName = new HashMap<>();
		try(Stream<Path> stream = Files.walk(folder))
		{
			filesByFileName = stream.filter(Files::isRegularFile)
					.collect(Collectors.toMap(path -> path.getFileName().toString().toLowerCase(),
							Function.identity(),
							(first, _) -> first));
		}
		catch(IOException e)
		{
			log.error("Cannot scan folder for media files", e);
		}

		for(MissingMediaEntry entry : entries)
		{
			final String oldMediaPath = entry.getOldMediaPath();
			if(oldMediaPath == null)
			{
				continue;
			}

			final Path foundFile = filesByFileName.get(Path.of(oldMediaPath).getFileName().toString().toLowerCase());
			if(foundFile != null)
			{
				entry.setMissingMediaSolutionType(MissingMediaSolutionType.REPLACE);
				entry.setNewMediaPath(foundFile.toString());
			}
		}
	}
}
