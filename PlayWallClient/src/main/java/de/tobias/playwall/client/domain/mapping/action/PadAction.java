package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@JsonTypeName("pad")
@ActionDescription(nameKey = "action.pad.name", order = 1, settingsViewController = PadActionSettingsViewController.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadAction implements Action, ActionStringify, ActionValidation
{
	public static final UUID ACTIVE_PAGE_ID = null;

	public enum PadActionMode
	{
		PLAY_STOP,
		PLAY_PAUSE,
		PLAY_PLAY,
		PLAY_HOLD
	}

	private PadActionMode padActionMode;

	private UUID pageId;

	private Integer position;

	@Override
	public Action copy()
	{
		return new PadAction(padActionMode, pageId, position);
	}

	@Override
	public boolean isValid(Project project)
	{
		if(pageId != null && project.getPage(pageId) == null)
		{
			return false;
		}

		return position != null;
	}

	@Override
	public String stringify(Project project)
	{
		if(position == null)
		{
			return Localization.getString("PadActionMode." + padActionMode);
		}

		if(pageId == ACTIVE_PAGE_ID)
		{
			return Localization.getString(Strings.ACTION_PAD_DETAILS_PAGE_ACTIVE, position + 1);
		}

		return Localization.getString(Strings.ACTION_PAD_DETAILS_PAGE_ID, position + 1, Optional.ofNullable(project.getPage(pageId))
				.map(page -> page.getSettings().getName())
				.orElse("?"));
	}
}
