package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.pad.ClientPadController;
import de.tobias.playwall.client.domain.pad.PadStatus;
import de.tobias.playwall.client.domain.project.ClientProjectController;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.view.toast.ToastAction;
import de.tobias.playwall.client.view.toast.ToastType;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ProjectLoadedListener implements UpdateMessageEventListener<ProjectLoadedUpdate>
{
	private final MainViewController mainViewController;
	private final ClientProjectController projectController;

	@Override
	public void onUpdateMessage(ProjectLoadedUpdate message)
	{
		mainViewController.getLoadingOverlay().hide();

		final List<ClientPadController> padControllersWithErrors = projectController.getPadControllersWithState(PadStatus.ERROR);
		if(!padControllersWithErrors.isEmpty())
		{
			Platform.runLater(() ->
					mainViewController.getMaterialToastManager().showPermanent(
							Localization.getString(Strings.UI_ERRORS_PROJECT_PAD_ERRORS_TITLE),
							Localization.getString(Strings.UI_ERRORS_PROJECT_PAD_ERRORS_MESSAGE, padControllersWithErrors.size()),
							ToastType.ERROR,
							new ToastAction(Localization.getString(Strings.UI_ERRORS_PROJECT_PAD_ERRORS_LINK), () -> mainViewController.onMenuItemReplaceMedia(null))));
		}
	}

	@Override
	public Class<ProjectLoadedUpdate> getMessageClass()
	{
		return ProjectLoadedUpdate.class;
	}
}
