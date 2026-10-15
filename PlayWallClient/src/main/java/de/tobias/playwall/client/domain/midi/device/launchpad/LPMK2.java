package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LPMK2 extends LPMKBase
{
	@Override
	public String getName()
	{
		return Localization.getString(Strings.LAUNCHPAD_MK3_MINI);
	}

	@Override
	public void onDeviceConnected(MidiDevice midiDevice)
	{
		log.debug("Launchpad MK2 configured");
	}
}
