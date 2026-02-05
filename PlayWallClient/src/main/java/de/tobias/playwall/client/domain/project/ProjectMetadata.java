package de.tobias.playwall.client.domain.project;

import de.tobias.playwall.client.view.style.color.ModernColor;
import de.tobias.playwall.common.api.common.TimeMode;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public final class ProjectMetadata
{
	private final UUID id;
	private String name;
	private Integer numberOfHorizontalPads;
	private Integer numberOfVerticalPads;
	private Double volume;
	private TimeMode timeMode;
	private ModernColor defaultColor;
	private ModernColor playColor;

	public int getNumberOfPadsPerPage()
	{
		return numberOfHorizontalPads * numberOfVerticalPads;
	}
}
