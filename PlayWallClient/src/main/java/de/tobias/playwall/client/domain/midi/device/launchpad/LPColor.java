package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.tobias.playwall.client.utils.Paintable;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LPColor implements Paintable
{
	WHITE(3, Color.rgb(255, 255, 255)),
	RED(5, Color.rgb(255, 0, 0)),
	ORANGE(9, Color.rgb(255, 127, 0)),
	LIME(13, Color.rgb(235, 255, 39)),
	LIGHT_GREEN(17, Color.rgb(123, 255, 66)),
	GREEN_1(21, Color.rgb(0, 255, 0)),
	GREEN_2(25, Color.rgb(62, 255, 112)),
	TURKEY_1(29, Color.rgb(62, 255, 112)),
	TURKEY_2(33, Color.rgb(101, 255, 196)),
	LIGHT_BLUE(37, Color.rgb(91, 255, 253)),
	BLUE(41, Color.rgb(69, 169, 255)),
	DARK_BLUE(45, Color.rgb(30, 67, 255)),
	PURPLE(49, Color.rgb(125, 73, 255)),
	VIOLET_1(53, Color.rgb(254, 85, 255)),
	VIOLET_2(57, Color.rgb(255, 75, 191)),
	BROWN(61, Color.rgb(255, 100, 69));

	private final int midiValue;
	private final Color color;

	public static LPColor fromMidiValue(int midiValue)
	{
		for(LPColor color : LPColor.values())
		{
			if(color.getMidiValue() == midiValue)
			{
				return color;
			}
		}
		throw new IllegalArgumentException("No LPColor found for " + midiValue);
	}
}
