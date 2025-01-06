package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.Pad;
import org.springframework.context.ApplicationContext;

public class AudioPadController extends PadController
{
	protected AudioPadController(ApplicationContext context, Pad pad)
	{
		super(context, pad);
	}

	@Override
	protected void _load()
	{

	}

	@Override
	protected void _unload()
	{

	}

	@Override
	public void play(boolean withFadeIn)
	{

	}

	@Override
	public boolean stop()
	{
		return false;
	}
}
