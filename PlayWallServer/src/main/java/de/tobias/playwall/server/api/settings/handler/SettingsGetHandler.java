package de.tobias.playwall.server.api.settings.handler;

import de.tobias.playwall.common.api.settings.SettingsGetRequest;
import de.tobias.playwall.common.api.settings.SettingsGetResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.settings.SettingsMapper;
import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.net.GetRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(SettingsGetRequest.class)
class SettingsGetHandler implements GetRequestHandler<SettingsGetRequest>
{
	private final SettingsService settingsService;
	private final SettingsMapper settingsMapper;

	@Override
	public Optional<ResponseMessage> handleRequest(SettingsGetRequest requestMessage) throws IOException
	{
		final Settings settings = settingsService.getSettings();
		return Optional.of(new SettingsGetResponse(requestMessage.getMessageId(), settingsMapper.settingsToSettingsDto(settings)));
	}
}