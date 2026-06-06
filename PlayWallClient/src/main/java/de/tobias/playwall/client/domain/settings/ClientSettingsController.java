package de.tobias.playwall.client.domain.settings;

import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.common.api.settings.audiodevices.AudioDeviceInstance;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class ClientSettingsController
{
	private Settings settings;

	private List<AudioDeviceInstance> outputDevices;
}
