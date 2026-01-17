package de.tobias.playwall.server.api.pad.handler;

import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadContentDto;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.pad.PadMapper;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.PadContent;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.api.history.UndoItem;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.net.UndoableRequestHandler;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

@RequestHandlerTyped(PadSettingsUpdateRequest.class)
public class PadSettingsUpdateHandler extends UndoableRequestHandler<PadSettingsUpdateRequest>
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
	public void handleUndoableRequest(PadSettingsUpdateRequest requestMessage) throws PlayWallServerException
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		if(pad == null)
		{
			final PadNotExistsError error = new PadNotExistsError(projectController.getLoadedProject().getMetadata().getId(), requestMessage.getPadId());
			throw new PlayWallServerException(messageSource, error);
		}

		pad.setName(requestMessage.getPad().getName());

		updatePadContent(requestMessage.getPad().getContent(), pad);

		context.publishEvent(new PadUpdate(padMapper.padToPadDto(pad)));
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

	@Override
	public UndoItem getInverseOperation(PadSettingsUpdateRequest requestMessage)
	{
		final Pad pad = projectController.getPad(requestMessage.getPadId());
		return new UndoItem("Kacheleinstellungen", requestMessage, new PadSettingsUpdateRequest(pad.getId(), padMapper.padToPadDto(pad)));
	}
}
