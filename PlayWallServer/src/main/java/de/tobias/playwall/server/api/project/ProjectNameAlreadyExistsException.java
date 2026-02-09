package de.tobias.playwall.server.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProjectNameAlreadyExistsException extends RuntimeException
{
	private final String name;
}
