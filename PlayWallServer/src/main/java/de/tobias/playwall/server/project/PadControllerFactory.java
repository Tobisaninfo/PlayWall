package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.Pad;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class PadControllerFactory
{
	public PadController createPadController(ApplicationContext context, Pad pad)
	{
		return switch(pad.getContentType())
		{
			case AUDIO -> new AudioPadController(context, pad);
		};
	}
}
