package de.tobias.playwall.client.log;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.ViewController;
import de.tobias.playwall.common.api.LogEntry;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

@ViewController(path = "de/tobias/playwall/client/view/log", view = "LogViewer")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class LogViewer extends NVC
{
	@FXML
	private ToggleButton btnTrace;
	@FXML
	private ToggleButton btnDebug;
	@FXML
	private ToggleButton btnInfo;
	@FXML
	private ToggleButton btnWarn;
	@FXML
	private ToggleButton btnError;
	@FXML
	private ToggleButton btnFatal;

	@FXML
	private ComboBox<String> sourceFilter;
	@FXML
	private TextField searchField;
	@FXML
	private TableView<LogEntry> table;
	@FXML
	private TableColumn<LogEntry, String> colTime;
	@FXML
	private TableColumn<LogEntry, String> colLevel;
	@FXML
	private TableColumn<LogEntry, String> colSource;
	@FXML
	private TableColumn<LogEntry, String> colLogger;
	@FXML
	private TableColumn<LogEntry, String> colMsg;
	@FXML
	private Label statusLabel;
	@FXML
	private Label connectedLabel;
	@FXML
	private Label portLabel;

	private final Set<LogEntry.Level> activeLevels = EnumSet.allOf(LogEntry.Level.class);
	private FilteredList<LogEntry> filteredEntries;
	private SortedList<LogEntry> sortedEntries;

	private final LogStore logStore;
	private ListChangeListener<LogEntry> logEntryListChangeListener;

	@Override
	public void init()
	{
		filteredEntries = new FilteredList<>(logStore.getAllEntries(), e -> true);
		sortedEntries = new SortedList<>(filteredEntries, Comparator.comparing(LogEntry::timestamp));

		colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormattedTime()));
		colLevel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().level().name()));
		colSource.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().source()));
		colLogger.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getShortLoggerName()));
		colMsg.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().message()));

		colLevel.setCellFactory(tc -> new LevelCell());

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setItems(sortedEntries);
		table.setRowFactory(_ -> {
			final TableRow<LogEntry> row = new TableRow<>();
			row.setOnMouseClicked(evt -> {
				if(evt.getClickCount() == 2 && !row.isEmpty())
					showDetail(row.getItem());
			});
			return row;
		});

		sourceFilter.setItems(logStore.getSources());
		sourceFilter.setValue(logStore.getSources().getFirst());

		statusLabel.textProperty().bind(Bindings.size(logStore.getAllEntries()).asString().map(s -> s + " Einträge"));

		searchField.textProperty().addListener((_, _, _) -> updateFilter(null));

		logEntryListChangeListener = c -> {
			if(!table.getItems().isEmpty())
			{
				table.scrollTo(table.getItems().size() - 1);
			}
		};
		logStore.getAllEntries().addListener(logEntryListChangeListener);

		for(ToggleButton b : new ToggleButton[]{btnTrace, btnDebug, btnInfo, btnWarn, btnError, btnFatal})
			b.setSelected(true);

		updateFilter(null);
	}

	@Override
	protected void initStage(NVCStage stageContainer, Stage stage)
	{
		stage.setTitle("Zentraler Log-Viewer");
		stage.setMinWidth(900);
		stage.setMinHeight(550);
		stage.getScene().getStylesheets().addFirst("style/logviewer.css");
		stage.setOnHidden(_ -> logStore.getAllEntries().removeListener(logEntryListChangeListener));
		stageContainer.addCloseKeyShortcut(stageContainer::close);
	}

	@FXML
	private void updateFilter(ActionEvent e)
	{
		activeLevels.clear();
		if(btnTrace.isSelected()) activeLevels.add(LogEntry.Level.TRACE);
		if(btnDebug.isSelected()) activeLevels.add(LogEntry.Level.DEBUG);
		if(btnInfo.isSelected()) activeLevels.add(LogEntry.Level.INFO);
		if(btnWarn.isSelected()) activeLevels.add(LogEntry.Level.WARN);
		if(btnError.isSelected()) activeLevels.add(LogEntry.Level.ERROR);
		if(btnFatal.isSelected()) activeLevels.add(LogEntry.Level.FATAL);

		String srcFilter = sourceFilter.getValue();
		String search = searchField.getText().toLowerCase();

		filteredEntries.setPredicate(entry -> {
			if(!activeLevels.contains(entry.level())) return false;
			if(srcFilter != null && !srcFilter.equals("Alle Quellen")
			   && !entry.source().equals(srcFilter)) return false;
			if(!search.isBlank() && !entry.message().toLowerCase().contains(search)) return false;
			return true;
		});
	}

	@FXML
	private void clearEntries()
	{
		logStore.clearMessages();
	}

	// ─── Detail Dialog ───────────────────────────────────────────────────────

	private void showDetail(LogEntry e)
	{
		Dialog<Void> dialog = new Dialog<>();
		dialog.setTitle("Log Detail");
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dialog.getDialogPane().setStyle("-fx-background-color: #161b22;");

		TextArea ta = new TextArea();
		ta.setEditable(false);
		ta.setStyle("-fx-control-inner-background: #0d1117; -fx-text-fill: #c9d1d9; -fx-font-family: Monospace;");
		ta.setPrefSize(700, 400);

		StringBuilder sb = new StringBuilder();
		sb.append("Zeit     : ").append(e.getFormattedTime()).append("\n");
		sb.append("Level    : ").append(e.level()).append("\n");
		sb.append("Anwendung: ").append(e.source()).append("\n");
		sb.append("Source   : ").append(e.loggerName()).append("#").append(e.method()).append(":").append(e.line()).append("\n\n");
		sb.append("Nachricht:\n").append(e.message());
		if(e.throwable() != null)
		{
			sb.append("\n\nException:\n").append(e.throwable());
		}
		ta.setText(sb.toString());

		dialog.getDialogPane().setContent(ta);
		dialog.showAndWait();
	}

	private static class LevelCell extends TableCell<LogEntry, String>
	{
		@Override
		protected void updateItem(String item, boolean empty)
		{
			super.updateItem(item, empty);
			getStyleClass().removeIf(s -> s.startsWith("cell-level-"));
			if(empty || item == null)
			{
				setText(null);
				return;
			}
			setText(item);
			getStyleClass().add("cell-level-" + item.toLowerCase());
		}
	}
}
