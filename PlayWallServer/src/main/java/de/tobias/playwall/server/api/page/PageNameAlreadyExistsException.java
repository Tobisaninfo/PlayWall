package de.tobias.playwall.server.api.page;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PageNameAlreadyExistsException extends Exception
{
	private final UUID pageId;
	private final String name;
}
