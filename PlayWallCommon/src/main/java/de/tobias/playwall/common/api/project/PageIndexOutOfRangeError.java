package de.tobias.playwall.common.api.project;

import de.tobias.playwall.common.api.ServerError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class PageIndexOutOfRangeError extends ServerError
{
	private int index;
	private int pageCount;

	@Override
	public Object[] getMessageArguments()
	{
		return new Object[]{index, pageCount};
	}
}
