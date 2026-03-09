package de.tobias.playwall.iconoverview;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IconUsageCategory
{
	UNDEFINED("?", "#CCCCCCFF", "#000000FF"),
	GENERAL("Allgemein", "#CA6702FF", "#000000FF"),
	PAD("Pad", "#134074FF", "#FFFFFFFF"),
	MENU("Menü", "#94D2BDFF", "#000000FF"),
	MAIN_WINDOW("Hauptfenster", "#E0BAD7FF", "#000000FF"),
	SETTINGS("Einstellungen", "#FFD166FF", "#000000FF"),
	DRAG_AND_DROP("Drag'n'Drop", "#FF0166FF", "#FFFFFFFF");

	private final String name;
	private final String backgroundColor;
	private final String fontColor;
}
