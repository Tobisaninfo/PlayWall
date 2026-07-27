package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.MappingRegistry;
import de.thecodelabs.midi.mapping.MappingSerializer;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;

@Configuration
public class MappingRegistryConfiguration
{
	@Bean
	public MappingSerializer mappingRegistry()
	{
		final MappingRegistry registry = new MappingRegistry();

		registry
				.registerInputKey(MidiInputKey.class)
				.registerInputKey(KeyboardInputKey.class);

		return registry.build();
	}
}
