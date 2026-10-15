package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LPMiniMK3 extends LPMKBase
{
	@Override
	public String getName()
	{
		return Localization.getString(Strings.LAUNCHPAD_MK3_MINI);
	}

	@Override
	public void onDeviceConnected(MidiDevice midiDevice)
	{
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 126, 127, 6, 1, (byte) 247}));
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 0, 32, 41, 2, 13, 0, 127, (byte) 247}));
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 0, 32, 41, 2, 13, 14, 1, (byte) 247}));

		log.debug("Launchpad MK3 configured");
	}
}
