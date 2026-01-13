package de.tobias.playwall.server.api.project.handler;

import de.tobias.playwall.common.api.project.GlobaleChangeVolumeRequest;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.model.project.*;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.PadController;
import de.tobias.playwall.server.project.ProjectController;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequestHandlerTyped(GlobaleChangeVolumeRequest.class)
@RequiredArgsConstructor
public class GlobalChangeVolumeHandler implements RequestHandler<GlobaleChangeVolumeRequest>
{
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(GlobaleChangeVolumeRequest requestMessage) throws IOException, PlayWallServerException
	{
		final Project loadedProject = projectController.getLoadedProject();
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
						padController.setVolume(requestMessage.getVolume() * audioPadContent.getVolume());
					}
				}
			}
		}

		return Optional.empty();
	}
}
