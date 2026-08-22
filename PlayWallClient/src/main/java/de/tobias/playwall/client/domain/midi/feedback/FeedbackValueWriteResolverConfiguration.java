package de.tobias.playwall.client.domain.midi.feedback;

import de.thecodelabs.midi.mapping.feedback.DefaultFeedbackValueWriterResolver;
import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriterResolver;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;

@Configuration
class FeedbackValueWriteResolverConfiguration
{
	@Bean
	FeedbackValueWriterResolver feedbackValueWriterResolver()
	{
		return new DefaultFeedbackValueWriterResolver();
	}
}
