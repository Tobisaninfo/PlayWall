package de.tobias.playwall.client.view.settings;

public interface Configurable<P>
{
	/**
	 * This method should return true, if the save action should be aborted (no settings applied, dialog remains open)
	 * For example {@link de.tobias.playwall.client.domain.settings.view.settings.ProgramSettingsAudioViewController} will abort on sound device change when any pad is playing.
	 *
	 * @param param old state of settings before anything is applied
	 * @return <code>true</code> - abort
	 */
	default boolean shouldApplySettingsAbort(P param)
	{
		return false;
	}

	void applySettings(P param);

	void cleanup();
}
