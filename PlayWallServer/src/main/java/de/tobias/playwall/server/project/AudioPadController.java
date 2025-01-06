package de.tobias.playwall.server.project;

import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.common.project.Pauseable;
import org.springframework.context.ApplicationContext;

public class AudioPadController extends PadController implements Pauseable
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
	public void pause()
	{

	}

	@Override
	public boolean stop()
	{
		return false;
	}

}
