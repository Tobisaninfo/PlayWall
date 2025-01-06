package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.AudioPad;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PadControllerFactory
{
	public PadController createPadController(ApplicationContext context, Pad pad)
	{
		return switch(pad)
		{
			case AudioPad audioPad -> new AudioPadController(context, audioPad);
		};
	}
}
