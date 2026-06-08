package de.tobias.playwall.iconoverview;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.tobias.playwall.iconoverview.parser.EnumUsageParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IconDeclaration
{
	@Getter
	private final ObservableList<IconEntry> data;

	private final Path sourceRootPath;

	public IconDeclaration(Path sourceRootPath)
	{
		this.sourceRootPath = sourceRootPath;
		this.data = initData();
	}

	@SuppressWarnings("java:S1117")
	private ObservableList<IconEntry> initData()
	{
		final ObservableList<IconEntry> data = FXCollections.observableArrayList();

		data.add(new IconEntry(FontAwesomeType.FLOPPY_DISK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Speichern Button"))));
		data.add(new IconEntry(FontAwesomeType.XMARK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Abbrechen Button"))));
		data.add(new IconEntry(FontAwesomeType.TRIANGLE_EXCLAMATION_SOLID, List.of(
				new IconUsage(IconUsageCategory.GENERAL, "Benachrichtigung"),
				new IconUsage(IconUsageCategory.PAD, "Medienfehler"),
				new IconUsage(IconUsageCategory.PAD, "Medium nicht gefunden"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein - Warnhinweise")
		)));
		data.add(new IconEntry(FontAwesomeType.PLUS_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Seite hinzufügen"))));
		data.add(new IconEntry(FontAwesomeType.PEN_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Seite umbenennen"))));
		data.add(new IconEntry(FontAwesomeType.COPY_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Seite duplizieren"))));
		data.add(new IconEntry(FontAwesomeType.PAINTBRUSH_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Farbmodus (globaler Colorpicker"))));
		data.add(new IconEntry(FontAwesomeType.FILE_IMPORT_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Projekt importieren"))));
		data.add(new IconEntry(FontAwesomeType.UP_RIGHT_FROM_SQUARE_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Projekt öffnen"))));

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
		data.add(new IconEntry(FontAwesomeType.FILE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Datei"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Im Ordner anzeigen"))));
		data.add(new IconEntry(FontAwesomeType.TRASH_CAN_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Entfernen"), new IconUsage(IconUsageCategory.MAIN_WINDOW, "Launch Dialog - Projekt entfernen"), new IconUsage(IconUsageCategory.MAIN_WINDOW, "Seite löschen"), new IconUsage(IconUsageCategory.PROJECT_MANAGEMENT, "Projekt löschen"))));
		data.add(new IconEntry(FontAwesomeType.VOLUME_HIGH_SOLID, List.of(
				new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Lautstärke"),
				new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Audio - Audiogerät"))));
		data.add(new IconEntry(FontAwesomeType.STOPWATCH_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Intro Dauer"))));
		data.add(new IconEntry(FontAwesomeType.IMAGE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige"))));
		data.add(new IconEntry(FontAwesomeType.CLOCK_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige - Zeitanzeige"))));
		data.add(new IconEntry(FontAwesomeType.PALETTE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige - Farbe"))));
		data.add(new IconEntry(FontAwesomeType.CIRCLE_ARROW_DOWN_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige - Farbe - Colorpicker"))));
		data.add(new IconEntry(FontAwesomeType.SLIDERS_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Fade-In/Out"))));
		data.add(new IconEntry(FontAwesomeType.ARROW_TREND_UP_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Fade-In"))));
		data.add(new IconEntry(FontAwesomeType.ARROW_TREND_DOWN_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Fade-Out"))));
		data.add(new IconEntry(FontAwesomeType.PEN_SOLID, List.of(new IconUsage(IconUsageCategory.PROJECT_MANAGEMENT, "Projekt löschen"))));
		data.add(new IconEntry(FontAwesomeType.CLONE_SOLID, List.of(new IconUsage(IconUsageCategory.PROJECT_MANAGEMENT, "Projekt klonen"))));
		data.add(new IconEntry(FontAwesomeType.ELLIPSIS_VERTICAL_SOLID, List.of(new IconUsage(IconUsageCategory.PROJECT_MANAGEMENT, "Mehr"))));

		// menu
		data.add(new IconEntry(FontAwesomeType.ARROWS_ROTATE_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Nach Updates suchen"))));
		data.add(new IconEntry(FontAwesomeType.CIRCLE_INFO_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Über"))));
		data.add(new IconEntry(FontAwesomeType.INBOX_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Logs"))));
		data.add(new IconEntry(FontAwesomeType.CLOCK_ROTATE_LEFT_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Zuletzt verwendete Projekte"))));
		data.add(new IconEntry(FontAwesomeType.EXPAND_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Vollbild"))));
		data.add(new IconEntry(FontAwesomeType.FILE_AUDIO_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Medienverwaltung"))));
		data.add(new IconEntry(FontAwesomeType.FILE_PEN_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Projekteinstellungen"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_PLUS_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Neues Projekt"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_TREE_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Projekte verwalten"))));
		data.add(new IconEntry(FontAwesomeType.HAND_POINTER_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Touchmodus aktivieren"))));
		data.add(new IconEntry(FontAwesomeType.ROTATE_LEFT_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Rückgängig"))));
		data.add(new IconEntry(FontAwesomeType.ROTATE_RIGHT_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Wiederholen"))));
		data.add(new IconEntry(FontAwesomeType.MAGNIFYING_GLASS_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "Kacheln suchen"))));
		data.add(new IconEntry(FontAwesomeType.GEAR_SOLID, List.of(
				new IconUsage(IconUsageCategory.MENU, "Einstellungen"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen"),
				new IconUsage(IconUsageCategory.PAD, "Einstellungen - Allgemein"))));
		data.add(new IconEntry(FontAwesomeType.THUMBTACK_SOLID, List.of(new IconUsage(IconUsageCategory.MENU, "im Vordergrund behalten"))));

		// settings
		data.add(new IconEntry(FontAwesomeType.PEN_TO_SQUARE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Name"))));
		data.add(new IconEntry(FontAwesomeType.TABLE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Anzahl Kacheln pro Seite"))));
		data.add(new IconEntry(FontAwesomeType.POWER_OFF_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Programmstart"))));
		data.add(new IconEntry(FontAwesomeType.ARROW_RIGHT_FROM_BRACKET_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Beenden des Programms"))));

		// File drag and drop
		data.add(new IconEntry(FontAwesomeType.MUSIC_SOLID, List.of(new IconUsage(IconUsageCategory.DRAG_AND_DROP, "Audio"))));

		// Toast
		data.add(new IconEntry(FontAwesomeType.INFO_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Toast Info"))));
		data.add(new IconEntry(FontAwesomeType.CHECK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Toast Success"))));
		data.add(new IconEntry(FontAwesomeType.CIRCLE_EXCLAMATION_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Toast Warning"))));
		data.add(new IconEntry(FontAwesomeType.CIRCLE_XMARK_SOLID, List.of(new IconUsage(IconUsageCategory.GENERAL, "Toast Error"))));

		// test unused
		data.add(new IconEntry(FontAwesomeType.CAKE_CANDLES_SOLID, List.of(new IconUsage(IconUsageCategory.UNDEFINED, "Ungenutzt"))));

		return data;
	}

	private Set<String> getDeclaredIconTypes()
	{
		return data.stream()
				.map(IconEntry::getFontIconType)
				.map(Object::toString)
				.collect(Collectors.toSet());
	}

	public Set<String> getUsedIconTypes()
	{
		try
		{
			final Set<String> usedValues = EnumUsageParser.parse(sourceRootPath, FontAwesomeType.class.getSimpleName());
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

	public Set<String> getNonDeclaredIconTypes()
	{
		final Set<String> nonDeclaredIconTypes = new HashSet<>(getUsedIconTypes());
		nonDeclaredIconTypes.removeAll(getDeclaredIconTypes());
		return nonDeclaredIconTypes;
	}

	public Set<String> getDeclaredButUnusedIconTypes()
	{
		final Set<String> declaredButUnusedIconTypes = new HashSet<>(getDeclaredIconTypes());
		declaredButUnusedIconTypes.removeAll(getUsedIconTypes());
		return declaredButUnusedIconTypes;
	}
}
