package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PadContentControllerFactory
{
	private final AudioHandlerFactory audioHandlerFactory;

	public PadController createPadContentController(ApplicationContext context, Pad pad)
	{
		return switch(pad.getContent())
		{
			case AudioPadContent audioContent ->
					new AudioPadContentController(context, pad, audioContent, audioHandlerFactory);
		};
	}
}
