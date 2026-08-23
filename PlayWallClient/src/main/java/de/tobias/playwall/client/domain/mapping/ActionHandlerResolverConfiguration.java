package de.tobias.playwall.client.domain.mapping;

import de.thecodelabs.midi.mapping.action.ActionHandlerResolver;
import de.thecodelabs.midi.mapping.action.DefaultActionHandlerResolver;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.domain.mapping.action.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
class ActionHandlerResolverConfiguration
{
	private ActionHandlerResolver actionHandlerResolver;

	private final PadActionHandler padActionHandler;
	private final PageActionHandler pageActionHandler;
	private final StopAllActionHandler stopAllActionHandler;
	private final GlobalVolumeActionHandler globalVolumeActionHandler;

	@PostConstruct
	private void init()
	{
		actionHandlerResolver = new DefaultActionHandlerResolver()
				.registerAction(PadAction.class, padActionHandler)
				.registerAction(PageAction.class, pageActionHandler)
				.registerAction(StopAllAction.class, stopAllActionHandler)
				.registerAction(GlobalVolumeAction.class, globalVolumeActionHandler);
	}

	@Bean
	public ActionHandlerResolver actionHandlerResolver()
	{
		return actionHandlerResolver;
	}
}
