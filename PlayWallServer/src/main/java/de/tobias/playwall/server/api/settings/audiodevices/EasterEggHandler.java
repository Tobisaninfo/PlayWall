package de.tobias.playwall.server.api.settings.audiodevices;

import de.tobias.playwall.common.api.settings.audiodevices.EasterEggRequest;
import de.tobias.playwall.server.api.settings.EasterEggService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(EasterEggRequest.class)
class EasterEggHandler implements OneTimeActionRequestHandler<EasterEggRequest>
{
	private final EasterEggService easterEggService;

	@Override
	public void handleRequest(EasterEggRequest requestMessage) throws IOException
	{
		easterEggService.play();
	}
}