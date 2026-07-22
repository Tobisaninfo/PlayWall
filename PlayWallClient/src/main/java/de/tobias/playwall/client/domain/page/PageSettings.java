package de.tobias.playwall.client.domain.page;

import de.tobias.playwall.client.view.style.color.ModernColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class PageSettings
{
	private String name;
	private ModernColor color;
}
