package de.tobias.playwall.common.api.page;

import de.tobias.playwall.common.api.ServerError;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PageNameAlreadyExistsError extends ServerError
{
	private String pageName;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{pageName};
	}
}
