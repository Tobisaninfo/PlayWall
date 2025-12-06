package de.tobias.playwall.client.viewcontroller.main.desktop;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.viewcontroller.main.PadView;
import de.tobias.playwall.client.viewcontroller.main.PadViewProvider;

@Service
public class DesktopPadViewProvider implements PadViewProvider
{
	@Override
	public PadView createNewPadView()
	{
		return new DesktopPadView();
	}
}
