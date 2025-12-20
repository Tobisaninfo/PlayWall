package de.tobias.playwall.server.api.project.handler;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playwall.common.api.project.PadNewMediaRequest;
import de.tobias.playwall.common.api.project.PadNewMediaResponse;
import de.tobias.playwall.common.net.ResponseMessage;
import de.tobias.playwall.common.utils.FileFormats;
import de.tobias.playwall.server.api.PlayWallServerException;
import de.tobias.playwall.server.common.model.project.AudioPadContent;
import de.tobias.playwall.server.common.model.project.Pad;
import de.tobias.playwall.server.common.model.project.PadContent;
import de.tobias.playwall.server.common.project.PadController;
import de.tobias.playwall.server.net.RequestHandler;
import de.tobias.playwall.server.net.RequestHandlerTyped;
import de.tobias.playwall.server.project.ProjectController;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@AllArgsConstructor
@RequestHandlerTyped(PadNewMediaRequest.class)
public class PadNewMediaHandler implements RequestHandler<PadNewMediaRequest>
{
	private final ProjectController projectController;

	@Override
	public Optional<ResponseMessage> handleRequest(PadNewMediaRequest requestMessage) throws IOException, PlayWallServerException
	{
		final PadController oldController = projectController.getPadController(requestMessage.getPadId());
		if(oldController != null)
		{
			oldController.stop();
			oldController.unload();
		}

		final Path path = Paths.get(requestMessage.getPath());
		final PadContent content = switch(FileFormats.getContentTypeForFile(path))
		{
			case AUDIO -> AudioPadContent.builder().mediaPath(path.toString()).build();
		};

		final Pad pad = projectController.getPad(requestMessage.getPadId());
		pad.setContent(content);

		final PadController newPadController = projectController.createNewPadController(pad);
		newPadController.load();

		pad.setName(PathUtils.getFilenameWithoutExtension(path.getFileName()));

		return Optional.of(new PadNewMediaResponse(requestMessage.getMessageId(), pad.getId(), pad.getName()));
	}
}
