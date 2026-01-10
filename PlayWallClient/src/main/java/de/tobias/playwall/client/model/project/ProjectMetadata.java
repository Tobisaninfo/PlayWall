package de.tobias.playwall.client.model.project;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class ProjectMetadata
{
	private final UUID id;
	private String name;
	private final int numberOfHorizontalPads;
	private final int numberOfVerticalPads;

	public int getNumberOfPadsPerPage()
	{
		return numberOfHorizontalPads * numberOfVerticalPads;
	}
}
