package de.tobias.playwall.client.viewcontroller;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.viewcontroller.style.Styleable;

public class BaseNVC extends NVC
{
	@InjectField
	protected Styleable styleable;
}
