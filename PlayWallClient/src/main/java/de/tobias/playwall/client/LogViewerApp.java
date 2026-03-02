package de.tobias.playwall.client;

import de.tobias.playwall.common.api.LogEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.EnumSet;
import java.util.Set;

public class LogViewerApp extends Application
{

	private static final int MAX_ENTRIES = 10_000;

	private final ObservableList<LogEntry> allEntries = FXCollections.observableArrayList();
	private FilteredList<LogEntry> filteredEntries;
	private TableView<LogEntry> table;

	private ComboBox<String> sourceFilter;
	private ToggleButton btnTrace, btnDebug, btnInfo, btnWarn, btnError, btnFatal;
	private TextField searchField;
	private Label statusLabel;
	private Label connectedLabel;

	private final Set<LogEntry.Level> activeLevels = EnumSet.allOf(LogEntry.Level.class);
	private int clientCount = 0;

	@Override
	public void start(Stage stage)
	{
		stage.setTitle("🔍 Zentraler Log-Viewer");
		stage.setMinWidth(900);
		stage.setMinHeight(550);

		filteredEntries = new FilteredList<>(allEntries, e -> true);

		BorderPane root = new BorderPane();
		root.setStyle("-fx-background-color: #0f1117;");

		root.setTop(buildToolbar());
		root.setCenter(buildTable());
		root.setBottom(buildStatusBar());

		Scene scene = new Scene(root, 1200, 700);
		scene.getStylesheets().add(getClass().getResource("/logviewer.css").toExternalForm());
		stage.setScene(scene);
		stage.show();

		startServer();
		updateFilter();
	}

	// ─── Toolbar ─────────────────────────────────────────────────────────────

	private HBox buildToolbar()
	{
		HBox bar = new HBox(10);
		bar.setPadding(new Insets(10, 14, 10, 14));
		bar.setAlignment(Pos.CENTER_LEFT);
		bar.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");

		Label title = new Label("LOG VIEWER");
		title.setFont(Font.font("Monospace", FontWeight.BOLD, 15));
		title.setStyle("-fx-text-fill: #58a6ff;");

		Region spacer1 = new Region();
		HBox.setHgrow(spacer1, Priority.ALWAYS);

		// Level toggle buttons
		btnTrace = levelBtn("TRACE", "#8b949e");
		btnDebug = levelBtn("DEBUG", "#58a6ff");
		btnInfo = levelBtn("INFO", "#3fb950");
		btnWarn = levelBtn("WARN", "#d29922");
		btnError = levelBtn("ERROR", "#f85149");
		btnFatal = levelBtn("FATAL", "#bc8cff");

		for(ToggleButton b : new ToggleButton[]{btnTrace, btnDebug, btnInfo, btnWarn, btnError, btnFatal})
		{
			b.setSelected(true);
			b.setOnAction(e -> updateFilter());
		}

		Region spacer2 = new Region();
		spacer2.setPrefWidth(12);

		// Source filter
		sourceFilter = new ComboBox<>();
		sourceFilter.getItems().add("Alle Quellen");
		sourceFilter.setValue("Alle Quellen");
		sourceFilter.setStyle(comboStyle());
		sourceFilter.setOnAction(e -> updateFilter());
		sourceFilter.setPrefWidth(160);

		// Search
		searchField = new TextField();
		searchField.setPromptText("🔍 Nachricht filtern...");
		searchField.setStyle(textFieldStyle());
		searchField.setPrefWidth(200);
		searchField.textProperty().addListener((obs, o, n) -> updateFilter());

		// Clear button
		Button btnClear = new Button("✕ Leeren");
		btnClear.setStyle(btnStyle("#21262d", "#f85149"));
		btnClear.setOnAction(e -> allEntries.clear());

		bar.getChildren().addAll(
				title, spacer1,
				new Label("Level:")
				{{
					setStyle("-fx-text-fill:#8b949e; -fx-font-size:11px;");
				}},
				btnTrace, btnDebug, btnInfo, btnWarn, btnError, btnFatal,
				spacer2,
				new Label("Quelle:")
				{{
					setStyle("-fx-text-fill:#8b949e; -fx-font-size:11px;");
				}},
				sourceFilter, searchField, btnClear
		);
		return bar;
	}

	// ─── Table ────────────────────────────────────────────────────────────────

	@SuppressWarnings("unchecked")
	private TableView<LogEntry> buildTable()
	{
		table = new TableView<>(filteredEntries);
		table.setStyle("-fx-background-color: #0f1117; -fx-control-inner-background: #0f1117;");
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<LogEntry, String> colTime = col("Zeit", 85,
				e -> new SimpleStringProperty(e.getFormattedTime()));
		TableColumn<LogEntry, String> colLevel = col("Level", 65,
				e -> new SimpleStringProperty(e.getLevel().name()));
		TableColumn<LogEntry, String> colSource = col("Anwendung", 120,
				e -> new SimpleStringProperty(e.getSource()));
		TableColumn<LogEntry, String> colLogger = col("Logger", 140,
				e -> new SimpleStringProperty(e.getShortLoggerName()));
		TableColumn<LogEntry, String> colMsg = col("Nachricht", -1,
				e -> new SimpleStringProperty(e.getMessage()));

		colLevel.setCellFactory(tc -> new LevelCell());

		table.getColumns().addAll(colTime, colLevel, colSource, colLogger, colMsg);

		// Detail popup on click
		table.setRowFactory(tv -> {
			TableRow<LogEntry> row = new TableRow<>();
			row.setOnMouseClicked(evt -> {
				if(evt.getClickCount() == 2 && !row.isEmpty())
				{
					showDetail(row.getItem());
				}
			});
			return row;
		});

		// Auto-scroll
		allEntries.addListener((javafx.collections.ListChangeListener<LogEntry>) c -> {
			if(!table.getItems().isEmpty())
			{
				table.scrollTo(table.getItems().size() - 1);
			}
		});

		return table;
	}

	// ─── Status Bar ──────────────────────────────────────────────────────────

	private HBox buildStatusBar()
	{
		HBox bar = new HBox(16);
		bar.setPadding(new Insets(5, 14, 5, 14));
		bar.setAlignment(Pos.CENTER_LEFT);
		bar.setStyle("-fx-background-color: #161b22; -fx-border-color: #30363d; -fx-border-width: 1 0 0 0;");

		statusLabel = new Label("0 Einträge");
		statusLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

		connectedLabel = new Label("● 0 Verbindungen");
		connectedLabel.setStyle("-fx-text-fill: #f85149; -fx-font-size: 11px;");

		Label portLabel = new Label("Port: " + LogServer.DEFAULT_PORT);
		portLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");

		bar.getChildren().addAll(connectedLabel, statusLabel, portLabel);
		return bar;
	}

	// ─── Server ──────────────────────────────────────────────────────────────

	private void startServer()
	{
		LogServer server = new LogServer(LogServer.DEFAULT_PORT, this::onEntry);

		// Override to track connections
		LogServer trackingServer = new LogServer(LogServer.DEFAULT_PORT, this::onEntry)
		{
			// We use the plain LogServer directly; override not possible without refactor.
		};

		server.start();
	}

	private void onEntry(LogEntry entry)
	{
		Platform.runLater(() -> {
			if(allEntries.size() >= MAX_ENTRIES)
			{
				allEntries.remove(0, 500);
			}
			allEntries.add(entry);

			// Track sources
			if(!sourceFilter.getItems().contains(entry.getSource()))
			{
				sourceFilter.getItems().add(entry.getSource());
			}

			statusLabel.setText(allEntries.size() + " Einträge");
		});
	}

	// ─── Filter ──────────────────────────────────────────────────────────────

	private void updateFilter()
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
			if(!activeLevels.contains(entry.getLevel())) return false;
			if(srcFilter != null && !srcFilter.equals("Alle Quellen")
			   && !entry.getSource().equals(srcFilter)) return false;
			if(!search.isBlank() && !entry.getMessage().toLowerCase().contains(search)) return false;
			return true;
		});
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
		sb.append("Level    : ").append(e.getLevel()).append("\n");
		sb.append("Anwendung: ").append(e.getSource()).append("\n");
		sb.append("Logger   : ").append(e.getLoggerName()).append("\n\n");
		sb.append("Nachricht:\n").append(e.getMessage());
		if(e.getThrowable() != null)
		{
			sb.append("\n\nException:\n").append(e.getThrowable());
		}
		ta.setText(sb.toString());

		dialog.getDialogPane().setContent(ta);
		dialog.showAndWait();
	}

	// ─── Helpers ─────────────────────────────────────────────────────────────

	private <T> TableColumn<LogEntry, T> col(String title, double prefW,
											 javafx.util.Callback<LogEntry, javafx.beans.value.ObservableValue<T>> vf)
	{
		TableColumn<LogEntry, T> c = new TableColumn<>(title);
		c.setCellValueFactory(data -> vf.call(data.getValue()));
		if(prefW > 0) c.setPrefWidth(prefW);
		return c;
	}

	private ToggleButton levelBtn(String text, String color)
	{
		ToggleButton b = new ToggleButton(text);
		b.setStyle("-fx-background-color: #21262d; -fx-text-fill: " + color
				   + "; -fx-font-family: Monospace; -fx-font-size: 11px; -fx-cursor: hand;"
				   + "-fx-border-radius: 4; -fx-background-radius: 4; -fx-border-color: #30363d; -fx-border-width: 1;");
		return b;
	}

	private String comboStyle()
	{
		return "-fx-background-color: #21262d; -fx-text-fill: #c9d1d9; -fx-border-color: #30363d;"
			   + "-fx-border-radius: 4; -fx-font-size: 12px;";
	}

	private String textFieldStyle()
	{
		return "-fx-background-color: #21262d; -fx-text-fill: #c9d1d9; -fx-prompt-text-fill: #8b949e;"
			   + "-fx-border-color: #30363d; -fx-border-radius: 4; -fx-font-size: 12px;";
	}

	private String btnStyle(String bg, String fg)
	{
		return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg
			   + "; -fx-border-color: " + fg + "; -fx-border-radius: 4; -fx-background-radius: 4;"
			   + "-fx-cursor: hand; -fx-font-size: 11px;";
	}

	public static void main(String[] args)
	{
		launch(args);
	}

	// ─── Level Cell ──────────────────────────────────────────────────────────

	private static class LevelCell extends TableCell<LogEntry, String>
	{
		@Override
		protected void updateItem(String item, boolean empty)
		{
			super.updateItem(item, empty);
			if(empty || item == null)
			{
				setText(null);
				setStyle("");
				return;
			}
			setText(item);
			String color = switch(item)
			{
				case "FATAL" -> "#bc8cff";
				case "ERROR" -> "#f85149";
				case "WARN" -> "#d29922";
				case "INFO" -> "#3fb950";
				case "DEBUG" -> "#58a6ff";
				default -> "#8b949e";
			};
			setStyle("-fx-text-fill: " + color + "; -fx-font-family: Monospace; -fx-font-weight: bold;");
		}
	}
}
