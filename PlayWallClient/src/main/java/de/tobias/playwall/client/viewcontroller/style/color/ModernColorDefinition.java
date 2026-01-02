package de.tobias.playwall.client.viewcontroller.style.color;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModernColorDefinition
{
	@Getter
	@Setter
	@NoArgsConstructor
	public static class Colors
	{
		private String hi;
		private String low;
		private String font;
		private String button;
		private Playbar playbar;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class Playbar
	{
		private String background;
		private String track;
	}

	private String name;
	private Colors colors;
}
