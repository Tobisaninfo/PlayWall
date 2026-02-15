package de.tobias.playwall.common.api;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class StackTraceError extends ServerError
{
	private String stackTrace;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[0];
	}
}
