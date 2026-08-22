package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.device.MidiListener;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;
import de.tobias.playwall.client.domain.midi.device.MidiDeviceRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeviceConfigurationMidiListener implements MidiListener
{
	private final MidiDeviceRegistry midiDeviceRegistry;

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
