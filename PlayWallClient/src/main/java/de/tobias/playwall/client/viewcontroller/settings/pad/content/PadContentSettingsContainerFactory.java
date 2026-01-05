package de.tobias.playwall.client.viewcontroller.settings.pad.content;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.model.project.AudioPadContent;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.model.project.PadContent;
import de.tobias.playwall.client.net.FluentClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@InjectConstructor}))
public class PadContentSettingsContainerFactory
{
	private final FluentClient fluentClient;

	public BasePadContentSettingsContainer<? extends PadContent> createPadContentSettingsContainer(Pad pad)
	{
		return switch(pad.getContent())
		{
			case AudioPadContent audioPadContent -> new AudioPadContentSettingsContainer(audioPadContent, pad.getId(), fluentClient);
		};
	}
}
