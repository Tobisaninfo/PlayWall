package de.tobias.playwall.iconoverview;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.iconoverview.parser.EnumUsageParser;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		stage.setMinWidth(500);
		stage.setMinHeight(400);
		stage.setWidth(1000);
		stage.setHeight(600);
		stage.setResizable(true);
		stage.centerOnScreen();

		labelTitle.setText(app.getInfo().getName());

		listView.setPlaceholder(new Label(getString("list.placeholder")));
		listView.setCellFactory(_ -> {
			final IconCell iconCell = new IconCell();

			iconCell.setOnMouseClicked(event -> {
				if(event.getClickCount() == 2)
				{
					iconCell.onDoubleClick();
				}
			});

			return iconCell;
		});

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
				filteredData.setPredicate(s -> s.getDescription().toLowerCase().contains(newValueLowerCase) || s.getFontIconType().toString().toLowerCase().contains(newValueLowerCase));
			}
		});
	}

	private ObservableList<IconEntry> getData()
	{
		final ObservableList<IconEntry> data = getDeclaredData();

		final Set<String> declaredIconTypes = data.stream()
				.map(IconEntry::getFontIconType)
				.map(Object::toString)
				.collect(Collectors.toSet());
		final Set<String> usedIconTypes = getUsedIconTypes();
		final Set<String> nonDeclaredIconTypes = new HashSet<>(usedIconTypes);
		nonDeclaredIconTypes.removeAll(declaredIconTypes);

		for(String nonDeclaredIconType : nonDeclaredIconTypes)
		{
			try
			{
				final FontAwesomeType fontIconType = FontAwesomeType.valueOf(nonDeclaredIconType);
				data.add(new IconEntry(fontIconType, List.of()));
			}
			catch(IllegalArgumentException e)
			{
				Logger.error(e);
			}
		}

		final Set<String> declaredButUnusedIconTypes = new HashSet<>(declaredIconTypes);
		declaredButUnusedIconTypes.removeAll(usedIconTypes);

		data.stream()
				.filter(entry -> declaredButUnusedIconTypes.contains(entry.getFontIconType().toString()))
				.forEach(entry -> entry.setUnused(true));

		FXCollections.sort(data, Comparator.comparing(e -> e.getFontIconType().toString()));

		return data;
	}

	private ObservableList<IconEntry> getDeclaredData()
	{
		final ObservableList<IconEntry> data = FXCollections.observableArrayList();
		data.add(new IconEntry(FontAwesomeType.FLOPPY_DISK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Speichern Button"))));
		data.add(new IconEntry(FontAwesomeType.XMARK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Abbrechen Button"))));
		data.add(new IconEntry(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID, List.of(
				new IconUsage(IconUsageCategory.GENERAL, "Benachrichtigung"),
				new IconUsage(IconUsageCategory.PAD, "Medienfehler"),
				new IconUsage(IconUsageCategory.PAD, "Medium nicht gefunden")
		)));

		// pad
		data.add(new IconEntry(FontAwesomeType.ARROW_ROTATE_LEFT_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel wiederholen (Loop)"))));
		data.add(new IconEntry(FontAwesomeType.PLAY_SOLID, List.of(
				new IconUsage(IconUsageCategory.PAD, "Kachel abspielen (Play)"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein - Wiedergabe - Loop")
		)));
		data.add(new IconEntry(FontAwesomeType.PAUSE_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel pausieren (Pause)"))));
		data.add(new IconEntry(FontAwesomeType.STOP_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel stoppen (Stop)"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_OPEN_SOLID, List.of(
				new IconUsage(IconUsageCategory.PAD, "Filechooser öffnen"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein - Datei")
		)));
		data.add(new IconEntry(FontAwesomeType.LINK_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Trigger für diese Kachel aktiv"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein - Im Ordner anzeigen"))));
		data.add(new IconEntry(FontAwesomeType.TRASH_CAN_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein - Entfernen"))));

		// menu
		data.add(new IconEntry(FontAwesomeType.ARROWS_ROTATE_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Nach Updates suchen"))));
		data.add(new IconEntry(FontAwesomeType.CIRCLE_INFO_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Über"))));
		data.add(new IconEntry(FontAwesomeType.CLOCK_ROTATE_LEFT_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Zuletzt verwendete Projekte"))));
		data.add(new IconEntry(FontAwesomeType.EXPAND_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Vollbild"))));
		data.add(new IconEntry(FontAwesomeType.FILE_AUDIO_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Medien ersetzen"))));
		data.add(new IconEntry(FontAwesomeType.FILE_PEN_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Projekteinstellungen"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_PLUS_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Neues Projekt"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_TREE_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Projekte verwalten"))));
		data.add(new IconEntry(FontAwesomeType.HAND_POINTER_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Touchmodus aktivieren"))));
		data.add(new IconEntry(FontAwesomeType.MAGNIFYING_GLASS_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Kacheln suchen"))));
		data.add(new IconEntry(FontAwesomeType.GEAR_SOLID, List.of(
				new IconUsage(IconUsageCategory.MENU, "Einstellungen"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein"))));
		data.add(new IconEntry(FontAwesomeType.THUMBTACK_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "im Vordergrund behalten"))));

		// settings
		data.add(new IconEntry(FontAwesomeType.PEN_TO_SQUARE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Name"))));
		data.add(new IconEntry(FontAwesomeType.TABLE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Anzahl Kacheln pro Seite"))));

		// test unused
		data.add(new IconEntry(FontAwesomeType.CAKE_CANDLES_SOLID, List.of(new IconUsage(IconUsageCategory.UNDEFINED, "Ungenutzt"))));

		return data;
	}

	private Set<String> getUsedIconTypes()
	{
		final Path sourceRoot = Paths.get(System.getProperty("user.dir") + "/PlayWallClient/src/main");
		try
		{
			final Set<String> usedValues = EnumUsageParser.parse(sourceRoot, FontAwesomeType.class.getSimpleName());
			for(String value : usedValues)
			{
				Logger.info(value);
			}
			return usedValues;
		}
		catch(IOException e)
		{
			Logger.error("Error parsing used icon types", e);
			return new HashSet<>();
		}
	}
}
