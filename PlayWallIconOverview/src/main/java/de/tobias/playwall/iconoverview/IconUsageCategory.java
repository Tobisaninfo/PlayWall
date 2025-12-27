package de.tobias.playwall.iconoverview;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IconUsageCategory
{
	UNDEFINED("", "#00000000", "#000000FF"),
	GENERAL("Allgemein", "#CA6702FF", "#000000FF"),
	PAD("Pad", "#134074FF", "#FFFFFFFF"),
	MENU("Menü", "#94D2BDFF", "#000000FF");

	private final String name;
	private final String backgroundColor;
	private final String fontColor;
}
