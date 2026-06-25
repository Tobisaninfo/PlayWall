package de.tobias.playwall.server.api.settings.audiodevices;

import de.tobias.playwall.common.api.settings.audiodevices.TestSoundPlayRequest;
import de.tobias.playwall.server.api.settings.TestSoundService;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@RequestHandlerTyped(TestSoundPlayRequest.class)
class TestSoundPlayHandler implements OneTimeActionRequestHandler<TestSoundPlayRequest>
{
	private final TestSoundService testSoundService;

	@Override
	public void handleRequest(TestSoundPlayRequest requestMessage) throws IOException
	{
		testSoundService.play(requestMessage.getAudioDevice());
	}
}