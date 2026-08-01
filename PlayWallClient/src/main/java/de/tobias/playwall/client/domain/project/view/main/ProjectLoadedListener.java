package de.tobias.playwall.client.domain.project.view.main;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playwall.client.Strings;
import de.tobias.playwall.client.domain.settings.ClientSettingsController;
import de.tobias.playwall.client.event.UpdateMessageEventListener;
import de.tobias.playwall.client.view.toast.ToastAction;
import de.tobias.playwall.client.view.toast.ToastType;
import de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import javafx.application.Platform;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ProjectLoadedListener implements UpdateMessageEventListener<ProjectLoadedUpdate>
{
	private final MainViewController mainViewController;
	private final ClientSettingsController settingsController;

	@Override
	public void onUpdateMessage(ProjectLoadedUpdate message)
	{
		mainViewController.getLoadingOverlay().hide();

		Platform.runLater(mainViewController::refreshPadErrorsToast);

		final String selectedAudioDevice = settingsController.getSettings().getSelectedAudioDevice();
		final List<AudioDeviceInstance> outputDevices = settingsController.getOutputDevices();
		final Optional<AudioDeviceInstance> matchingAudioDevice = outputDevices.stream()
				.filter(d -> d.name().equals(selectedAudioDevice))
				.findFirst();
		if(selectedAudioDevice != null && matchingAudioDevice.isEmpty())
		{
			Platform.runLater(() ->
					mainViewController.getMaterialToastManager().showPermanent(
							Localization.getString(Strings.UI_ERRORS_AUDIO_DEVICE_ERRORS_TITLE),
							Localization.getString(Strings.UI_ERRORS_AUDIO_DEVICE_ERRORS_MESSAGE, selectedAudioDevice),
							ToastType.ERROR,
							new ToastAction(Localization.getString(Strings.UI_ERRORS_AUDIO_DEVICE_ERRORS_LINK), () -> mainViewController.onMenuItemSettings(null))));
		}
	}

	@Override
	public Class<ProjectLoadedUpdate> getMessageClass()
	{
		return ProjectLoadedUpdate.class;
	}
}
