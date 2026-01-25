package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.pad.AudioPadContentDto;
import de.tobias.playwall.common.api.pad.PadContentDto;
import de.tobias.playwall.common.api.pad.request.PadNotExistsError;
import de.tobias.playwall.common.api.pad.request.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.pad.update.PadUpdate;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.pad.PadContent;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Optional;

@RequestHandlerTyped(PadSettingsUpdateRequest.class)
class PadSettingsUpdateHandler implements UndoableRequestHandler<PadSettingsUpdateRequest>
{
	private final ProjectController projectController;

	private final ApplicationContext context;
	private final PadMapper padMapper;

	private final MessageSource messageSource;

	public PadSettingsUpdateHandler(ProjectController projectController, ApplicationContext context, PadMapper padMapper, MessageSource messageSource)
	{
		this.projectController = projectController;
		this.context = context;
		this.padMapper = padMapper;
		this.messageSource = messageSource;
	}

	@Override
	public Optional<UndoItem> handleRequest(PadSettingsUpdateRequest requestMessage) throws PlayWallServerException
	{
		final UndoItem inverseOperation = getInverseOperation(requestMessage);

		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		pad.setName(requestMessage.getPad().getName());
		pad.setTimeMode(requestMessage.getPad().getTimeMode());

		updatePadContent(requestMessage.getPad().getContent(), pad);

		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));

		return Optional.of(inverseOperation);
	}

	private void updatePadContent(PadContentDto requestPadContent, Pad pad)
	{
		final PadContent padContentToUpdate = pad.getContent();

		if(padContentToUpdate == null)
		{
			return;
		}

		switch(padContentToUpdate)
		{
			case AudioPadContent audioPadContent ->
			{
				final AudioPadContentDto requestAudioPadContent = (AudioPadContentDto) requestPadContent;

				audioPadContent.setLoop(requestAudioPadContent.isLoop());
				audioPadContent.setVolume(requestAudioPadContent.getVolume());

				final PadController padController = projectController.getPadController(pad.getId());
				if(padController != null)
				{
					padController.setLooping(requestAudioPadContent.isLoop());
					padController.setVolume(requestAudioPadContent.getVolume());
				}
			}
		}
	}

	private UndoItem getInverseOperation(PadSettingsUpdateRequest requestMessage)
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		final String shortDescription = messageSource.getMessage("undo.description.short.pad.settings", new Object[]{}, LocaleContextHolder.getLocale());
		final String pageName = projectController.getPageByPad(pad.getId()).getName();
		final String longDescription = messageSource.getMessage("undo.description.long.pad.settings", new Object[]{pad.getPosition() + 1, pageName}, LocaleContextHolder.getLocale());
		return new UndoItem(shortDescription, longDescription, requestMessage, new PadSettingsUpdateRequest(pad.getId(), padMapper.padToPadDto(pad)));
	}
}
