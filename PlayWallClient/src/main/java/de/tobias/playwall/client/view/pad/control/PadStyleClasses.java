package de.tobias.playwall.client.view.pad.control;


import de.tobias.playwall.client.model.project.PadIndex;

public class PadStyleClasses
{

	private PadStyleClasses()
	{
	}

	public static final String STYLE_CLASS_PAD = "pad";
	public static final String STYLE_CLASS_PAD_INDEX = "pad${index}";

	public static final String STYLE_CLASS_PAD_BUTTON = "pad-button";
	public static final String STYLE_CLASS_PAD_BUTTON_INDEX = "pad${index}-button";

	public static final String STYLE_CLASS_PAD_ICON = "pad-icon";
	public static final String STYLE_CLASS_PAD_ICON_INDEX = "pad${index}-icon";

	public static final String STYLE_CLASS_PAD_INFO = "pad-info";
	public static final String STYLE_CLASS_PAD_INFO_INDEX = "pad${index}-info";

	public static final String STYLE_CLASS_PAD_TITLE = "pad-title";
	public static final String STYLE_CLASS_PAD_TITLE_INDEX = "pad${index}-title";

	public static final String STYLE_CLASS_PAD_BUTTON_BOX = "pad-button-box";
	public static final String STYLE_CLASS_PAD_BUTTON_ROOT = "pad-root";

	public static final String STYLE_CLASS_PAD_PLAYBAR = "pad-playbar";
	public static final String STYLE_CLASS_PAD_PLAYBAR_INDEX = "pad${index}-playbar";

	public static final String STYLE_CLASS_PAD_CUE_IN = "pad-cue-in";
	public static final String STYLE_CLASS_PAD_CUE_IN_INDEX = "pad${index}-cue-in";

	public static String replaceIndex(String styleClass, PadIndex index)
	{
		return styleClass.replace("${index}", String.valueOf(index));
	}
}
