package de.tobias.playwall.server.api.settings.handler;

import de.tobias.playwall.common.api.settings.SettingsUpdateRequest;
import de.tobias.playwall.common.api.settings.update.SettingsUpdate;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.settings.SettingsMapper;
import de.tobias.playwall.server.api.settings.SettingsService;
import de.tobias.playwall.server.common.model.settings.Settings;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(SettingsUpdateRequest.class)
class SettingsUpdateHandler extends UndoableRequestHandler<SettingsUpdateRequest>
{
	private final SettingsService settingsService;
	private final SettingsMapper settingsMapper;
	private final ProjectController projectController;

	public SettingsUpdateHandler(MessageSource messageSource, ApplicationContext context, SettingsService settingsService, SettingsMapper settingsMapper, ProjectController projectController)
	{
		super(messageSource, context);
		this.settingsService = settingsService;
		this.settingsMapper = settingsMapper;
		this.projectController = projectController;
	}

	@Override
	public Optional<UndoItem> handleRequest(SettingsUpdateRequest requestMessage) throws IOException
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage, settingsService.getSettings());

		final String previousSelectedAudioDevice = settingsService.getSettings().getSelectedAudioDevice();
		final Settings settings = settingsMapper.settingsDtoToSettings(requestMessage.getSettings());
		settingsService.updateSettings(settings);

		final String selectedDevice = settings.getSelectedAudioDevice();
		if(selectedDevice != null && !selectedDevice.equals(previousSelectedAudioDevice))
		{
			projectController.setOutputDeviceForAll(selectedDevice);
		}

		context.publishEvent(new SettingsUpdate(settingsMapper.settingsToSettingsDto(settings)));

		return Optional.of(inverseOperation);
	}

	private UndoItem getInverseOperation(SettingsUpdateRequest request, Settings oldSettings)
	{
		final String shortDescription = messageSource.getMessage("undo.description.short.program.settings", new Object[]{}, LocaleContextHolder.getLocale());
		final String longDescription = messageSource.getMessage("undo.description.long.program.settings", new Object[]{}, LocaleContextHolder.getLocale());
		return new UndoItem(shortDescription, longDescription, request, new SettingsUpdateRequest(settingsMapper.settingsToSettingsDto(oldSettings)));
	}
}