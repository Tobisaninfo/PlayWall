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
	public static final double MIN_VOLUME = 0.0;
	public static final double MAX_VOLUME = 1.15;
	private static final double DEFAULT_VOLUME = 1.0;

	private final int VERSION = 1;

	public interface List
	{
	}

	@JsonView(List.class)
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
}
