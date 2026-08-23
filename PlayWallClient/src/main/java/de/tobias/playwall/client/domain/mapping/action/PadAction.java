package de.tobias.playwall.client.domain.mapping.action;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.thecodelabs.midi.mapping.action.Action;
import de.thecodelabs.utils.util.Localization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonTypeName("pad")
@ActionDescription(nameKey = "action.pad.name", order = 1, settingsViewController = PadActionSettingsViewController.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PadAction implements Action
{
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
	public String toString()
	{
		if(position == null)
		{
			return Localization.getString("PadActionMode." + padActionMode);
		}

		return Localization.getString("action.pad.details", position + 1, Localization.getString("PadActionMode." + padActionMode));
	}
}
