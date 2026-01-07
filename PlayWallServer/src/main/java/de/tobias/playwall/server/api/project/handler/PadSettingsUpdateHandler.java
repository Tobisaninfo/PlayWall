package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.PadNotExistsError;
import de.tobias.playwall.common.api.project.PadSettingsUpdateRequest;
import de.tobias.playwall.common.api.project.PadUpdate;
import de.tobias.playwall.common.api.project.model.AudioPadContentDto;
import de.tobias.playwall.common.api.project.model.PadContentDto;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.api.project.PadMapper;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.PadContent;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(PadSettingsUpdateRequest.class)
public class PadSettingsUpdateHandler implements RequestHandler<PadSettingsUpdateRequest>
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
	public Optional<ResponseMessage> handleRequest(PadSettingsUpdateRequest requestMessage) throws IOException, PlayWallServerException
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

		return Optional.empty();
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
}
