package de.tobias.playwall.server.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageIndexOutOfRangeException extends RuntimeException
{
	private final int index;
	private final int pageCount;
}
