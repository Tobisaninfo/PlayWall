package de.tobias.playwall.client.domain.midi.feedback;

import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriter;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;

public class DefaultFeedbackValueWriter implements FeedbackValueWriter<MidiInputKey, DefaultFeedbackValue>
{
	private final Midi midi;

	public DefaultFeedbackValueWriter(Midi midi)
	{
		this.midi = midi;
	}

	@Override
	public void write(MidiInputKey key, DefaultFeedbackValue value)
	{
		midi.getDevice().sendMidiMessage(new MidiMessage(MidiMessageType.NOTE_ON, (byte) 0, key.value(), (byte) value.value()));
	}
}
