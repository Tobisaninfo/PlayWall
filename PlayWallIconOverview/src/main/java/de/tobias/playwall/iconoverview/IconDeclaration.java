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
				new IconUsage(IconUsageCategory.PAD, "Medium nicht gefunden")
		)));
		data.add(new IconEntry(FontAwesomeType.PLUS_SOLID, List.of(new IconUsage(IconUsageCategory.MAIN_WINDOW, "Seite hinzufügen"))));

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
		data.add(new IconEntry(FontAwesomeType.TRASH_CAN_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Entfernen"))));
		data.add(new IconEntry(FontAwesomeType.VOLUME_HIGH_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Allgemein - Lautstärke"))));
		data.add(new IconEntry(FontAwesomeType.IMAGE_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige"))));
		data.add(new IconEntry(FontAwesomeType.CLOCK_SOLID, List.of(new IconUsage(IconUsageCategory.SETTINGS, "Einstellungen - Anzeige - Zeitanzeige"))));

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
