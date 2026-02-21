package de.tobias.playwall.server.api.settings.handler;

import de.tobias.playwall.common.api.settings.SettingsUpdateRequest;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.settings.SettingsMapper;
import de.tobias.playwall.server.api.settings.SettingsRepository;
import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(SettingsUpdateRequest.class)
@RequiredArgsConstructor
class SettingsUpdateHandler implements UndoableRequestHandler<SettingsUpdateRequest>
{
	private final SettingsRepository settingsRepository;
	private final MessageSource messageSource;
	private final ApplicationContext context;

	private final SettingsMapper settingsMapper;

	@Override
	public Optional<UndoItem> handleRequest(SettingsUpdateRequest requestMessage) throws IOException
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

		final Settings settings = settingsMapper.settingsDtoToSettings(requestMessage.getSettings());
		settingsRepository.saveSettings(settings);

		context.publishEvent(new SettingsUpdate(settingsMapper.settingsToSettingsDto(settings)));

		return Optional.of(inverseOperation);
	}

	private UndoItem getInverseOperation(SettingsUpdateRequest request)
	{
		final Settings oldSettings = settingsMapper.settingsDtoToSettings(request.getSettings());
		final String shortDescription = messageSource.getMessage("undo.description.short.program.settings", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.program.settings", new Object[]{}, LocaleContextHolder.getLocale());
		return new UndoItem(shortDescription, longDescription, request, new SettingsUpdateRequest(settingsMapper.settingsToSettingsDto(oldSettings)));
	}
}