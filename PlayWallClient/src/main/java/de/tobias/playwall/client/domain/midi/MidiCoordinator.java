package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.event.MidiDeviceSelected;
import de.tobias.playwall.client.event.EventListener;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiUnavailableException;
import java.util.Optional;

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
			log.info("Close MIDI device: {}", oldDevice.getMidiDeviceInfo());
			oldDevice.closeDevice();
		}

		if(event.getDeviceName() != null)
		{
			try
			{
				final Optional<MidiDeviceInfo> midiDeviceInfoOptional = midi.getMidiDeviceInfo(event.getDeviceName());
				if(midiDeviceInfoOptional.isEmpty())
				{
					log.error("Could not find MIDI device with name: {}", event.getDeviceName());
					return;
				}
				final MidiDevice device = midi.openDevice(midiDeviceInfoOptional.get(), Midi.Mode.INPUT);
				log.info("Open MIDI device: {}", device.getMidiDeviceInfo());
				device.getPublisher().addMidiListener(e -> log.debug("Received MIDI event: {}", e));
			}
			catch(MidiUnavailableException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
