package de.tobias.playwall.client.domain.midi.device;

import de.thecodelabs.midi.mapping.feedback.FeedbackValue;
import de.thecodelabs.midi.midi.device.MidiListener;

public abstract class CustomMidiDevice implements MidiListener
{
	public abstract Class<? extends FeedbackValue> supportedFeedbackValue();

	public abstract String getName();
}
