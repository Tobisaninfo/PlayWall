package de.tobias.playwall.server.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ProjectNotExistsException extends RuntimeException
{
	private final UUID projectId;
}
