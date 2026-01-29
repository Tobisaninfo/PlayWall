package de.tobias.playwall.client.view.style.color;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.thecodelabs.utils.application.ApplicationUtils;
import javafx.scene.paint.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public enum ModernColor
{
	RED1,
	RED2,
	RED3,

	DARK_RED1,
	DARK_RED2,
	DARK_RED3,

	PINK1,
	PINK2,
	PINK3,

	PURPLE1,
	PURPLE2,
	PURPLE3,

	LIGHT_BLUE1,
	LIGHT_BLUE2,
	LIGHT_BLUE3,

	BLUE1,
	BLUE2,
	BLUE3,

	LIGHT_GREEN1,
	LIGHT_GREEN2,
	LIGHT_GREEN3,

	LIME1,
	LIME2,
	LIME3,

	YELLOW1,
	YELLOW2,
	YELLOW3,

	ORANGE1,
	ORANGE2,
	ORANGE3,

	GRAY1,
	GRAY2,
	GRAY3,
	GRAY4,
	GRAY5,
	GRAY6;

	private static final ModernColorDefinition[] colors;

	static
	{
		final InputStream inputStream = ApplicationUtils.getApplication().getClasspathResource("style", "colors", "ModernColor.json").getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			colors = mapper.readValue(inputStream, ModernColorDefinition[].class);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ModernColorDefinition getCurrentModernColor()
	{
		return Stream.of(colors)
				.filter(color -> color.getName().equals(name())).findAny()
				.orElseThrow(IllegalArgumentException::new);
	}

	public String getColorHi()
	{
		return getCurrentModernColor().getColors().getHi();
	}

	public String getColorLow()
	{
		return getCurrentModernColor().getColors().getLow();
	}

	public Color getColor()
	{
		return Color.web(paint());
	}

	public String getFontColor()
	{
		return getCurrentModernColor().getColors().getFont();
	}

	public String getButtonColor()
	{
		return getCurrentModernColor().getColors().getButton();
	}

	public String getPlaybarColor()
	{
		return getCurrentModernColor().getColors().getPlaybar().getBackground();
	}

	public String getPlaybarTrackColor()
	{
		return getCurrentModernColor().getColors().getPlaybar().getTrack();
	}

	public String paint()
	{
		return getColorLow();
	}

	public static ModernColor modernColorByBackgroundColor(String color)
	{
		for(ModernColor modernColor : ModernColor.values())
		{
			if(modernColor.getColorHi().contains(color) || modernColor.getColorLow().contains(color))
			{
				return modernColor;
			}
		}
		return null;
	}
}
