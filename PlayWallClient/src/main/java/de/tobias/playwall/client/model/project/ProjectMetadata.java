package de.tobias.playwall.client.model.project;

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
	private final int numberOfHorizontalPads;
	private final int numberOfVerticalPads;
	private double volume;

	public int getNumberOfPadsPerPage()
	{
		return numberOfHorizontalPads * numberOfVerticalPads;
	}
}
