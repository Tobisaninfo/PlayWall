package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriter;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;

public class LPFeedbackValueWriter implements FeedbackValueWriter<MidiInputKey, LPFeedbackValue>
{
	private final Midi midi;

	public LPFeedbackValueWriter(Midi midi)
	{
		this.midi = midi;
	}

	@Override
	public void write(MidiInputKey key, LPFeedbackValue value)
	{
		midi.getDevice().sendMidiMessage(new MidiMessage(MidiMessageType.NOTE_ON, (byte) LPMiniMK3.FEEDBACK_LIGHT, key.value(), (byte) value.getValue()));
	}
}
