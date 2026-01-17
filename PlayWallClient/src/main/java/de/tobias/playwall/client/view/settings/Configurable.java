package de.tobias.playwall.client.view.settings;

public interface Configurable<P>
{
	void applySettings(P param);

	void cleanup();
}
