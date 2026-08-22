package de.tobias.playwall.client.domain.midi.feedback;

import de.thecodelabs.midi.mapping.feedback.DefaultFeedbackValueWriterResolver;
import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriterResolver;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.thecodelabs.midi.midi.Midi;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPFeedbackValue;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPFeedbackValueWriter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
class FeedbackValueWriteResolverConfiguration
{
	private final Midi midi;

	@Bean
	FeedbackValueWriterResolver feedbackValueWriterResolver()
	{
		return new DefaultFeedbackValueWriterResolver()
				.registerFeedbackValue(MidiInputKey.class, LPFeedbackValue.class, new LPFeedbackValueWriter(midi));
	}
}
