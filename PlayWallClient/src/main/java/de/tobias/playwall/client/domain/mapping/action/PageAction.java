package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;

@JsonTypeName("page")
@ActionDescription(nameKey = "action.page.name")
public class PageAction implements Action
{
	@Override
	public Action copy()
	{
		return new PageAction();
	}
}
