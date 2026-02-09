package de.tobias.playwall.server.api.pad;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PadNotExistsException extends RuntimeException
{
	private final UUID projectId;
	private final UUID padId;
}
