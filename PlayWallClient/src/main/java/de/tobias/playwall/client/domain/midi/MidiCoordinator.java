package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.event.MidiDeviceSelected;
import de.tobias.playwall.client.event.EventListener;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiUnavailableException;

@Service
@AllArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
@Slf4j
public class MidiCoordinator
{
	private final Midi midi;

	@EventListener(MidiDeviceSelected.class)
	void onDeviceSelected(MidiDeviceSelected event)
	{
		final MidiDevice oldDevice = midi.getDevice();
		if(oldDevice != null)
		{
			oldDevice.closeDevice();
		}

		if(event.getMidiDeviceInfo() != null)
		{
			try
			{
				final MidiDevice device = midi.openDevice(event.getMidiDeviceInfo(), Midi.Mode.INPUT);
				device.getPublisher().addMidiListener(e -> log.debug("Received MIDI event: {}", e));
			}
			catch(MidiUnavailableException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
