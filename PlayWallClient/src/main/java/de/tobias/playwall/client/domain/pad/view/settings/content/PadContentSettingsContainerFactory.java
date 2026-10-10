package de.tobias.playwall.client.domain.pad.view.settings.content;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.AudioPadContent;
import de.tobias.playwall.client.domain.pad.Pad;
import de.tobias.playwall.client.domain.pad.PadContent;
import de.tobias.playwall.client.domain.pad.view.settings.PadSettingsViewController;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.domain.project.Project;
import de.tobias.playwall.client.domain.project.ProjectMetadata;
import de.tobias.playwall.client.net.FluentClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @InjectConstructor)
public class PadContentSettingsContainerFactory
{
	private final FluentClient fluentClient;
	private final ClientProjectController clientProjectController;

	@SuppressWarnings("java:S1452")
	public BasePadContentSettingsContainer<? extends PadContent> createPadContentSettingsContainer(Pad pad, PadSettingsViewController parentDialog)
	{
		PadContent padContent = pad.getContent();
		if(padContent == null)
		{
			return new FallbackPadContentSettingsContainer(pad.getId(), parentDialog, getProjectMetadata());
		}

		return switch(padContent)
		{
			case AudioPadContent audioPadContent ->
					new AudioPadContentSettingsContainer(audioPadContent, pad.getId(), fluentClient, parentDialog, getProjectMetadata());
		};
	}

	private ProjectMetadata getProjectMetadata()
	{
		return Optional.ofNullable(clientProjectController.getProject())
				.map(Project::getMetadata)
				.orElse(null);
	}
}
