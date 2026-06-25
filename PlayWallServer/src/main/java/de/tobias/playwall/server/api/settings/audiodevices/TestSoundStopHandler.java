package de.tobias.playwall.server.api.settings.audiodevices;

import de.tobias.playwall.common.api.settings.audiodevices.TestSoundStopRequest;
import de.tobias.playwall.server.api.settings.TestSoundService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(TestSoundStopRequest.class)
class TestSoundStopHandler implements OneTimeActionRequestHandler<TestSoundStopRequest>
{
	private final TestSoundService testSoundService;

	@Override
	public void handleRequest(TestSoundStopRequest requestMessage) throws IOException
	{
		testSoundService.stop();
	}
}