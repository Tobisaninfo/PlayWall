package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.Color;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor(access = AccessLevel.PACKAGE, onConstructor_ = @InjectConstructor)
public class ColorMapper
{
	public ModernColor colorToModernColor(Color color)
	{
		if(color == null)
		{
			return null;
		}

		return ModernColor.valueOf(color.name());
	}

	public Color modernColorToColor(ModernColor color)
	{
		if(color == null)
		{
			return null;
		}

		return Color.valueOf(color.name());
	}
}
