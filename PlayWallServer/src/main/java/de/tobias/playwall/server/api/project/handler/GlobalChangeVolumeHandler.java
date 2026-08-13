package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.request.GlobalChangeVolumeRequest;
import de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate;
import de.tobias.playwall.server.api.project.ProjectMetadataMapper;
import de.tobias.playwall.server.common.audio.VolumeHelper;
import de.tobias.playwall.server.common.model.pad.AudioPadContent;
import de.tobias.playwall.server.common.model.pad.Pad;
import de.tobias.playwall.server.common.model.pad.PadContent;
import de.tobias.playwall.server.common.model.page.Page;
import de.tobias.playwall.server.common.model.project.Project;
import de.tobias.playwall.server.net.OneTimeActionRequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@RequestHandlerTyped(GlobalChangeVolumeRequest.class)
@RequiredArgsConstructor
class GlobalChangeVolumeHandler implements OneTimeActionRequestHandler<GlobalChangeVolumeRequest>
{
	private final ProjectController projectController;
	private final ApplicationContext context;
	private final ProjectMetadataMapper projectMetadataMapper;

	@Override
	public void handleRequest(GlobalChangeVolumeRequest requestMessage) throws IOException
	{
		final Project loadedProject = projectController.getLoadedProject();

		VolumeHelper.validateVolume(requestMessage.getVolume());

		loadedProject.getMetadata().setVolume(requestMessage.getVolume());

		for(Page page : loadedProject.getPages())
		{
			for(Pad pad : page.getPads())
			{
				final PadContent padContent = pad.getContent();

				if(padContent instanceof AudioPadContent audioPadContent)
				{
					final PadController padController = projectController.getPadController(pad.getId());
					if(padController != null)
					{
						padController.setVolume(audioPadContent.getVolume());
					}
				}
			}
		}

		context.publishEvent(new ProjectSettingsUpdate(projectMetadataMapper.projectMetadataToProjectMetadataDto(loadedProject.getMetadata())));
	}
}
