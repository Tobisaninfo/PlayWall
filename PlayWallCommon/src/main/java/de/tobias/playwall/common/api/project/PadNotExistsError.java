package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PadNotExistsError extends ServerError
{
	private UUID projectId;
	private UUID padId;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{padId};
	}
}
