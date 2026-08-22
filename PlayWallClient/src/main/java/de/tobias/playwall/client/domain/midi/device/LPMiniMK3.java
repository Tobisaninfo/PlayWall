package de.tobias.playwall.client.domain.midi.device;

import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class LPMiniMK3 extends CustomMidiDevice
{
	@Override
	public Class<? extends FeedbackValue> supportedFeedbackValue()
	{
		return LPFeedbackValue.class;
	}

	@Override
	public void onDeviceOpen(MidiDevice midiDevice)
	{
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 126, 127, 6, 1, (byte) 247}));
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 0, 32, 41, 2, 13, 0, 127, (byte) 247}));
		midiDevice.sendMidiMessage(new MidiMessage(new byte[]{(byte) 240, 0, 32, 41, 2, 13, 14, 1, (byte) 247}));

		log.debug("Launchpad MK3 configured");
	}

	@Override
	public void onFeedbackClear(MidiDevice midiDevice)
	{
		final int maxMainKeyNumber = 89;

		for(byte i = 11; i <= maxMainKeyNumber; i++)
		{
			midiDevice.sendMidiMessage(new MidiMessage(MidiMessageType.NOTE_ON, i, (byte) 0));
		}

		// Obere Reihe an Tasten
		final int liveKeyMin = 104;
		final int liveKeyMax = 111;

		for(byte i = liveKeyMin; i <= liveKeyMax; i++)
		{
			midiDevice.sendMidiMessage(new MidiMessage(MidiMessageType.CONTROL_CHANGE, i, (byte) 0));
		}
	}
}
