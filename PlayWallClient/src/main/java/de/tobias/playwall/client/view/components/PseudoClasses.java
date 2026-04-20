package de.tobias.playwall.client.view.components;

import javafx.css.PseudoClass;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PseudoClasses
{
	public static final PseudoClass ERROR_CLASS = PseudoClass.getPseudoClass("error");
	public static final PseudoClass PLAY_CLASS = PseudoClass.getPseudoClass("play");
	public static final PseudoClass FADE_CLASS = PseudoClass.getPseudoClass("fade");
	public static final PseudoClass HOVER_CLASS = PseudoClass.getPseudoClass("hover");
	public static final PseudoClass DRAG_CLASS = PseudoClass.getPseudoClass("drag");

	public static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

	public static final PseudoClass SUCCESS_CLASS = PseudoClass.getPseudoClass("success");
	public static final PseudoClass WARNING_CLASS = PseudoClass.getPseudoClass("warning");
	public static final PseudoClass DANGER_CLASS = PseudoClass.getPseudoClass("danger");
}
