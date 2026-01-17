package de.tobias.playwall.server.common.model.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class ProjectMetadata
{
	private static final double DEFAULT_VOLUME = 1.0;

	private final int VERSION = 1;

	public interface List
	{
	}

	@JsonView(List.class)
	@EqualsAndHashCode.Exclude
	private UUID id;

	@JsonView(List.class)
	private String name;

	private int numberOfHorizontalPads;
	private int numberOfVerticalPads;

	@Builder.Default
	private double volume = DEFAULT_VOLUME;

	@JsonIgnore
	public int getNumberOfPadsPerPage()
	{
		return numberOfHorizontalPads * numberOfVerticalPads;
	}

	public ProjectMetadata copy()
	{
		return ProjectMetadata.builder()
				.id(UUID.randomUUID())
				.name(name)
				.numberOfHorizontalPads(numberOfHorizontalPads)
				.numberOfVerticalPads(numberOfVerticalPads)
				.volume(volume)
				.build();
	}
}
