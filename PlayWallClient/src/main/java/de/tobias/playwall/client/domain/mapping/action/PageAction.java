package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeName("page")
@ActionDescription(nameKey = "action.page.name", settingsViewController = PageActionSettingsViewController.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageAction implements Action
{
	public enum PageActionType
	{
		PREVIOUS, NEXT, JUMP
	}

	private PageActionType pageActionType;
	private int pageNumber;

	@Override
	public Action copy()
	{
		return new PageAction(pageActionType, pageNumber);
	}
}
