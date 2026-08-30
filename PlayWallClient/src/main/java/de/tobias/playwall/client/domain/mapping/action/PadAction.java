package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import de.tobias.playwall.client.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@JsonTypeName("pad")
@ActionDescription(
		nameKey = "action.pad.name",
		order = 1,
		feedbackTypes = {DefaultFeedbackState.NORMAL, DefaultFeedbackState.ACTIVE, DefaultFeedbackState.WARNING},
		settingsViewController = PadActionSettingsViewController.class
)
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
		final String positionString = Optional.ofNullable(position)
				.map(i -> i + 1)
				.map(String::valueOf)
				.orElse("?");

		if(pageId == ACTIVE_PAGE_ID)
		{
			return Localization.getString(Strings.ACTION_PAD_DETAILS_PAGE_ACTIVE, positionString);
		}

		return Localization.getString(Strings.ACTION_PAD_DETAILS_PAGE_ID, positionString, Optional.ofNullable(project.getPage(pageId))
				.map(page -> page.getSettings().getName())
				.orElse("?"));
	}
}
