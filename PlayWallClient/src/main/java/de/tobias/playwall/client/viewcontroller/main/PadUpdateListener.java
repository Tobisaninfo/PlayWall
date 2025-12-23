package de.tobias.playwall.client.viewcontroller.main;

import de.tobias.playwall.client.appcontext.AppContextHolder;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.mapper.PadMapper;
import de.tobias.playwall.client.model.project.Pad;
import de.tobias.playwall.client.service.ProjectService;
import de.tobias.playwall.common.api.project.PadUpdate;
import javafx.application.Platform;

class PadUpdateListener implements UpdateMessageEventListener<PadUpdate>
{
	private final ProjectService projectService;
	private final MainViewController mainViewController;
	private final PadMapper padMapper;

	public PadUpdateListener(MainViewController mainViewController)
	{
		this.projectService = AppContextHolder.getInstance().get(ProjectService.class);
		this.mainViewController = mainViewController;
		this.padMapper = AppContextHolder.getInstance().get(PadMapper.class);
	}

	@Override
	public void onUpdateMessage(PadUpdate message)
	{
		final Pad newPad = padMapper.padDtoToPad(message.getPad());
		projectService.updatePad(mainViewController.getProject(), newPad);

		final PadView padView = mainViewController.getPadViewForPadId(message.getPad().getId());
		if(padView != null)
		{
			Platform.runLater(() -> padView.updateFromPad(newPad));
		}
	}

	@Override
	public Class<PadUpdate> getMessageClass()
	{
		return PadUpdate.class;
	}
}
