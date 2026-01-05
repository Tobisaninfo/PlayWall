package de.tobias.playwall.client.viewcontroller.settings;

public interface Configurable<P>
{
	void applySettings(P param);

	void cleanup();
}
