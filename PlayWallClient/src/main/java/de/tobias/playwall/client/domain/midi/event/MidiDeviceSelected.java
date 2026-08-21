package de.tobias.playwall.client.domain.midi.event;

import de.tobias.playwall.common.net.UpdateMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MidiDeviceSelected extends UpdateMessage
{
	private final String deviceName;
}
