package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.FallbackPadContent;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import de.tobias.playwall.client.viewcontroller.settings.pad.PadSettingsViewController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@InjectConstructor}))
public class PadContentSettingsContainerFactory
{
	private final FluentClient fluentClient;

	public BasePadContentSettingsContainer<? extends PadContent> createPadContentSettingsContainer(Pad pad, PadSettingsViewController parentDialog)
	{
		PadContent padContent = pad.getContent();
		if(padContent == null)
		{
			padContent = FallbackPadContent.builder().build();
		}

		return switch(padContent)
		{
			case AudioPadContent audioPadContent ->
					new AudioPadContentSettingsContainer(audioPadContent, pad.getId(), fluentClient, parentDialog);
			case FallbackPadContent _ ->
					new FallbackPadContentSettingsContainer(pad.getId(), fluentClient, parentDialog);
		};
	}
}
