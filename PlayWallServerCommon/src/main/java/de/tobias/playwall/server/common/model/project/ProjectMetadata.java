package de.tobias.playwall.server.common.model.project;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class ProjectMetadata
{
	private UUID id;
	private String name;
	private int numberOfHorizontalPads;
	private int numberOfVerticalPads;
}
