package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonTypeName("page")
@ActionDescription(nameKey = "action.page.name", order = 2, settingsViewController = PageActionSettingsViewController.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageAction implements Action
{
	public enum PageActionMode
	{
		PREVIOUS, NEXT, JUMP
	}

	private PageActionMode pageActionMode;
	private Integer pageNumber;

	@Override
	public Action copy()
	{
		return new PageAction(pageActionMode, pageNumber);
	}

	@Override
	public String toString()
	{
		String string = Localization.getString("PageActionMode." + pageActionMode);
		if(pageActionMode == PageActionMode.JUMP)
		{
			string += " " + pageNumber;
		}
		return string;
	}
}
