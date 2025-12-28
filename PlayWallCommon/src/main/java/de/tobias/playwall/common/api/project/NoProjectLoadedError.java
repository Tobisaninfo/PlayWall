package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString(callSuper = true)
public class NoProjectLoadedError extends ServerError
{
	@Override
	public Object[] getMessageArguments()
	{
		return new Object[0];
	}
}
