package de.tobias.playwall.client.domain.midi.device;

import de.thecodelabs.midi.mapping.feedback.FeedbackState;
import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.midi.midi.device.MidiListener;

public abstract class CustomMidiDevice implements MidiListener
{
	public abstract Class<? extends FeedbackValue> supportedFeedbackValueForState(FeedbackState state);

	public abstract String getName();
}
