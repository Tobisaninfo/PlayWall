package de.tobias.playwall.client.domain.midi.device.launchpad;

import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriter;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.message.MidiMessage;
import de.thecodelabs.midi.midi.message.MidiMessageType;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;

public class LPAnimatedFeedbackValueWriter implements FeedbackValueWriter<MidiInputKey, LPAnimatedFeedbackValue>
{
	private final Midi midi;

	public LPAnimatedFeedbackValueWriter(Midi midi)
	{
		this.midi = midi;
	}

	@Override
	public void write(MidiInputKey key, LPAnimatedFeedbackValue value)
	{
		if(value.isFlashing())
		{
			final LPFeedbackValue activeFeedbackValue = (LPFeedbackValue) key.getFeedbackValueForState(DefaultFeedbackState.ACTIVE);
			midi.getDevice().sendMidiMessage(new MidiMessage(MidiMessageType.NOTE_ON, (byte) LPMiniMK3.FEEDBACK_PULSE, key.value(), (byte) activeFeedbackValue.getValue()));
		}
	}
}
