package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.MappingRegistry;
import de.thecodelabs.midi.mapping.MappingSerializer;
import de.thecodelabs.midi.mapping.action.ActionHandlerResolver;
import de.thecodelabs.midi.mapping.input.KeyboardInputKey;
import de.thecodelabs.midi.mapping.input.MidiInputKey;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.domain.mapping.action.PageAction;
import de.tobias.playwall.client.domain.mapping.action.PageActionHandler;
import de.tobias.playwall.client.domain.mapping.action.StopAllAction;
import de.tobias.playwall.client.domain.mapping.action.StopAllActionHandler;

@Configuration
public class MappingRegistryConfiguration
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
				.registerAction(PageAction.class, new PageActionHandler())
				.registerAction(StopAllAction.class, new StopAllActionHandler());
	}

	@Bean
	public MappingSerializer mappingSerializer()
	{
		return registry.build();
	}

	@Bean
	public ActionHandlerResolver actionHandlerResolver()
	{
		return registry;
	}

	@Bean
	public MappingRegistry mappingRegistry()
	{
		return registry;
	}
}
