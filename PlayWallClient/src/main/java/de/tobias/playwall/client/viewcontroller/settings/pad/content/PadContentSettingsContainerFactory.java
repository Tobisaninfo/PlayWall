package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.PadContent;

public class PadContentSettingsContainerFactory
{
	public static BasePadContentSettingsContainer<? extends PadContent> createPadContentSettingsContainer(PadContent padContent)
	{
		return switch(padContent)
		{
			case AudioPadContent audioPadContent -> new AudioPadContentSettingsContainer(audioPadContent);
		};
	}
}
