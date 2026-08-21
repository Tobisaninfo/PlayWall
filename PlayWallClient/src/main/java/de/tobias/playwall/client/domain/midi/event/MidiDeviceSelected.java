package de.tobias.playwall.client.domain.midi.event;

import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.tobias.playwall.common.net.UpdateMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MidiDeviceSelected extends UpdateMessage
{
	private final MidiDeviceInfo midiDeviceInfo;
}
