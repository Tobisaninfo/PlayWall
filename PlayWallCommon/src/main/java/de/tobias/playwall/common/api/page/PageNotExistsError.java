package de.tobias.playwall.common.api.page;

import de.tobias.playwall.common.api.ServerError;
import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PageNotExistsError extends ServerError
{
	private UUID pageId;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{pageId};
	}
}
