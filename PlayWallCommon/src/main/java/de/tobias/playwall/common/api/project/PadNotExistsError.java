package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PadNotExistsError extends ServerError
{
	private UUID projectId;
	private UUID padId;
}
