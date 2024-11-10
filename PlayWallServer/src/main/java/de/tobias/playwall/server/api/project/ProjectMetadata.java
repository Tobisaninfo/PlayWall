package de.tobias.playwall.server.api.project;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectMetadata
{
	private UUID id;
	private String name;
}
