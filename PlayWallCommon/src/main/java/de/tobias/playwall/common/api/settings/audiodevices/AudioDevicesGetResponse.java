package de.tobias.playwall.common.api.settings.audiodevices;

import de.tobias.playwall.common.net.ResponseMessage;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(callSuper = true)
public class AudioDevicesGetResponse extends ResponseMessage
{
	private List<AudioDeviceInstance> audioDevices;

	public AudioDevicesGetResponse(UUID messageId, List<AudioDeviceInstance> audioDevices)
	{
		super(messageId);
		this.audioDevices = audioDevices;
	}
}
