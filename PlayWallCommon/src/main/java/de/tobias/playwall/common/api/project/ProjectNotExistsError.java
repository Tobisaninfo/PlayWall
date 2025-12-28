package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class ProjectNotExistsError extends ServerError
{
	private UUID projectId;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{projectId};
	}
}
