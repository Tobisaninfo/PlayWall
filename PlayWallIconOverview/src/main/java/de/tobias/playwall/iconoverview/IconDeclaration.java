package de.tobias.playwall.iconoverview;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IconDeclaration
{
	public static ObservableList<IconEntry> getDeclaredData()
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
		data.add(new IconEntry(FontAwesomeType.PLAY_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel abspielen (Play)"))));
		data.add(new IconEntry(FontAwesomeType.PAUSE_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel pausieren (Pause)"))));
		data.add(new IconEntry(FontAwesomeType.STOP_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Kachel stoppen (Stop)"))));
		data.add(new IconEntry(FontAwesomeType.FOLDER_OPEN_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Filechooser öffnen"))));
		data.add(new IconEntry(FontAwesomeType.LINK_SOLID, List.of(new IconUsage(IconUsageCategory.PAD, "Trigger für diese Kachel aktiv"))));

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
}
