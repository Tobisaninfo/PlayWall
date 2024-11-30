package de.tobias.playwall.server.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PageNotExistsException extends Exception
{
	private final UUID projectId;
	private final UUID pageId;
}
