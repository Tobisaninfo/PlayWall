package de.tobias.playwall.client.viewcontroller.style;

public class ModernStyleSizeHelper
{

	private static final double MIN_WIDTH = 140;
	private static final double MIN_HEIGHT = 115;

	private ModernStyleSizeHelper() {
	}

	public static double getMinHeight(int rows) {
		return rows * MIN_HEIGHT;
	}

	public static double getMinWidth(int columns) {
		return columns * MIN_WIDTH;
	}

	public static double getPadHeight() {
		return MIN_HEIGHT;
	}

	public static double getPadWidth() {
		return MIN_WIDTH;
	}
}
