package de.tobias.playwall.server.common.model.page;

import de.tobias.playwall.common.api.common.Color;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
public class PageSettings
{
	private String name;
	private Color color;

	public PageSettings copy()
	{
		return PageSettings.builder()
				.name(name)
				.color(color)
				.build();
	}
}
