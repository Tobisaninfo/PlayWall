package de.tobias.playwall.server.common.model.project;

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
	public interface List
	{
	}

	@JsonView(List.class)
	private UUID id;
	@JsonView(List.class)
	private String name;
	private int numberOfHorizontalPads;
	private int numberOfVerticalPads;
}
