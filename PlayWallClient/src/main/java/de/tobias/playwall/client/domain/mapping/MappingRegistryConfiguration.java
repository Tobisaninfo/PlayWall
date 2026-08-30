package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.MappingRegistry;
import de.thecodelabs.midi.mapping.MappingSerializer;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.domain.mapping.action.GlobalVolumeAction;
import de.tobias.playwall.client.domain.mapping.action.PadAction;
import de.tobias.playwall.client.domain.mapping.action.PageAction;
import de.tobias.playwall.client.domain.mapping.action.StopAllAction;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPAnimatedFeedbackValue;
import de.tobias.playwall.client.domain.midi.feedback.DefaultFeedbackState;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPFeedbackValue;

@Configuration
class MappingRegistryConfiguration
{
	private MappingRegistry registry;

	@PostConstruct
	private void init()
	{
		registry = new MappingRegistry();

		registry
				.registerInputKey(MidiInputKey.class)
				.registerInputKey(KeyboardInputKey.class);

		registry
				.registerAction(PadAction.class)
				.registerAction(PageAction.class)
				.registerAction(StopAllAction.class)
				.registerAction(GlobalVolumeAction.class);

		registry.registerFeedbackState(DefaultFeedbackState.class)
				.registerFeedbackValue(LPFeedbackValue.class)
				.registerFeedbackValue(LPAnimatedFeedbackValue.class);
	}

	@Bean
	public MappingSerializer mappingSerializer()
	{
		return registry.build();
	}

	@Bean
	public MappingRegistry mappingRegistry()
	{
		return registry;
	}
}
