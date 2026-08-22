package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.device.MidiListener;
import de.tobias.playwall.client.domain.midi.device.CustomMidiDeviceRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomMidiDeviceListener implements MidiListener
{
	private final CustomMidiDeviceRegistry midiDeviceRegistry;

	@Override
	public void onDeviceOpen(MidiDevice midiDevice)
	{
		midiDeviceRegistry.lookup(midiDevice.getMidiDeviceInfo().name())
				.ifPresent(midiListener -> midiListener.onDeviceOpen(midiDevice));
	}

	@Override
	public void onFeedbackClear(MidiDevice midiDevice)
	{
		midiDeviceRegistry.lookup(midiDevice.getMidiDeviceInfo().name())
				.ifPresent(midiListener -> midiListener.onFeedbackClear(midiDevice));
	}
}
