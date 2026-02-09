package de.tobias.playwall.server.api.page;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PageNotExistsException extends RuntimeException
{
	private final UUID projectId;
	private final UUID pageId;
}
