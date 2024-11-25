package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ProjectNameAlreadyExistsError extends ServerError
{
	private String projectName;
}
