package de.tobias.playwall.client.domain.midi.device;

import de.thecodelabs.midi.mapping.feedback.FeedbackClearWriter;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomMidiDeviceClearWriter implements FeedbackClearWriter<MidiInputKey>
{
	private final Midi midi;

	@Override
	public void write(MidiInputKey midiInputKey)
	{
		midi.clearFeedbackForKey(midiInputKey);
	}
}
