package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.audio.AudioHandlerFactory;
import de.tobias.playwall.server.common.model.project.AudioPad;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PadControllerFactory
{
	private final AudioHandlerFactory audioHandlerFactory;

	public PadController createPadController(ApplicationContext context, Pad pad)
	{
		return switch(pad)
		{
			case AudioPad audioPad -> new AudioPadController(context, audioPad, audioHandlerFactory);
		};
	}
}
