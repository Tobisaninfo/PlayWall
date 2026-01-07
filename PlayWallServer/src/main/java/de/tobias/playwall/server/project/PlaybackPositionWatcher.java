package de.tobias.playwall.server.project;

import de.tobias.playwall.common.api.project.PadPlayPositionUpdate;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
class PlaybackPositionWatcher
{
	private final ProjectController projectController;
	private final ApplicationContext context;

	@Scheduled(fixedRate = 50)
	void run()
	{
		final List<PadController> controllers = projectController.getPlayingPadControllers();
		if(controllers.isEmpty())
		{
			return;
		}

		List<PadPlayPositionUpdate.PadPlayPosition> positions = controllers.stream()
				.map(controller -> new PadPlayPositionUpdate.PadPlayPosition(controller.getPad().getId(), controller.getPlayPosition().toMillis()))
				.toList();
		context.publishEvent(new PadPlayPositionUpdate(positions));
	}
}
