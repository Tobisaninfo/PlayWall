package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.midi.device.CustomMidiDevice;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LPMiniMK3 extends CustomMidiDevice
{
	@Override
	public String getName()
	{
		return Localization.getString(Strings.LAUNCHPAD_MK3_MINI);
	}

	@Override
	public Class<? extends FeedbackValue> supportedFeedbackValueForState(FeedbackState state)
	{
		if(state == DefaultFeedbackState.NORMAL || state == DefaultFeedbackState.ACTIVE)
		{
			return LPFeedbackValue.class;
		}
		return null;
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
		final int liveKeyMin = 91;
		final int liveKeyMax = 98;

		for(byte i = liveKeyMin; i <= liveKeyMax; i++)
		{
			midiDevice.sendMidiMessage(new MidiMessage(MidiMessageType.CONTROL_CHANGE, i, (byte) 0));
		}
	}

	@Override
	public void onFeedbackClear(MidiDevice midiDevice, MidiInputKey key)
	{
		final byte note = key.value();

		if(note < 91)
		{
			midiDevice.sendMidiMessage(new MidiMessage(MidiMessageType.NOTE_ON, note, (byte) 0));
		}
		else
		{
			midiDevice.sendMidiMessage(new MidiMessage(MidiMessageType.CONTROL_CHANGE, note, (byte) 0));
		}
	}
}
