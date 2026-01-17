package de.tobias.playwall.client.domain.pad.view.desktop;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.pad.view.PadView;
import de.tobias.playwall.client.domain.pad.view.PadViewProvider;

@Service(superclass = PadViewProvider.class)
public class DesktopPadViewProvider implements PadViewProvider
{
	@Override
	public PadView createNewPadView()
	{
		return new DesktopPadView();
	}
}
