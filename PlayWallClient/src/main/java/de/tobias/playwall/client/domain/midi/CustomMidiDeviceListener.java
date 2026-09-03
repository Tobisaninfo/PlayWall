package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.device.MidiListener;
import de.tobias.playwall.client.domain.midi.device.CustomMidiDeviceRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomMidiDeviceListener implements MidiListener
{
	private final CustomMidiDeviceRegistry midiDeviceRegistry;

	@Override
	public void onDeviceConnected(MidiDevice midiDevice)
	{
		midiDeviceRegistry.lookup(midiDevice.getMidiDeviceInfo().name())
				.ifPresent(midiListener -> midiListener.onDeviceConnected(midiDevice));
	}

	@Override
	public void onFeedbackClear(MidiDevice midiDevice)
	{
		midiDeviceRegistry.lookup(midiDevice.getMidiDeviceInfo().name())
				.ifPresent(midiListener -> midiListener.onFeedbackClear(midiDevice));
	}

	@Override
	public void onFeedbackClearForKey(MidiDevice midiDevice, MidiInputKey midiInputKey)
	{
		midiDeviceRegistry.lookup(midiDevice.getMidiDeviceInfo().name())
				.ifPresent(midiListener -> midiListener.onFeedbackClearForKey(midiDevice, midiInputKey));
	}
}
