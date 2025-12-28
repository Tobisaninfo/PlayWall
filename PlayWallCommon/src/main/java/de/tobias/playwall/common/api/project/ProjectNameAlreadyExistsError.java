package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectNameAlreadyExistsError extends ServerError
{
	private String projectName;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{projectName};
	}
}
