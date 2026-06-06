package de.tobias.playwall.server.project;

import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.common.model.settings.Settings;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PadContentControllerFactory
{
	private final AudioHandlerFactory audioHandlerFactory;
	private final SettingsService settingsService;

	public PadController createPadContentController(ApplicationContext context, Project project, Pad pad)
	{
		final Settings settings = settingsService.getSettings();

		return switch(pad.getContent())
		{
			case AudioPadContent audioContent ->
					new AudioPadContentController(context, pad, audioContent, audioHandlerFactory, project, settings.getSelectedAudioDevice());
		};
	}
}
