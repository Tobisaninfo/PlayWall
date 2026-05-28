package de.tobias.playwall.server.api.settings.audiodevices;

import de.tobias.playwall.common.api.settings.audiodevices.AudioDevicesGetRequest;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDevicesGetResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.nativeaudio.audio.rust.AudioDevice;
import de.tobias.playwall.nativeaudio.audio.rust.RustAudioHandler;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(AudioDevicesGetRequest.class)
class AudioDevicesGetHandler implements GetRequestHandler<AudioDevicesGetRequest>
{
	@Override
	public Optional<ResponseMessage> handleRequest(AudioDevicesGetRequest requestMessage) throws IOException
	{
		final List<String> deviceNames = Arrays.stream(RustAudioHandler.getOutputDevices())
				.map(AudioDevice::name)
				.sorted()
				.toList();
		return Optional.of(new AudioDevicesGetResponse(requestMessage.getMessageId(), deviceNames));
	}
}