package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.mapping.Mapping;
import de.thecodelabs.midi.mapping.action.ActionHandlerResolver;
import de.thecodelabs.midi.mapping.feedback.FeedbackValueWriterResolver;
import de.thecodelabs.midi.mapping.listener.MidiMappingListener;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.device.MidiDevice;
import de.thecodelabs.midi.midi.device.MidiDeviceInfo;
import de.tobias.playwall.client.domain.midi.event.MidiDeviceSelected;
import de.tobias.playwall.client.event.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiUnavailableException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MidiCoordinator
{
	private final Midi midi;
	private final ActionHandlerResolver actionHandlerResolver;
	private final FeedbackValueWriterResolver feedbackValueWriterResolver;

	private MidiMappingListener midiMappingListener;

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
				final MidiDevice device = midi.openDevice(midiDeviceInfoOptional.get(), Midi.Mode.INPUT, Midi.Mode.OUTPUT);
				log.info("Open MIDI device: {}", device.getMidiDeviceInfo());
			}
			catch(MidiUnavailableException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public void showCurrentFeedback(Mapping mapping)
	{
		if(midi.isOpen())
		{
			midi.showCurrentFeedbackState(mapping, actionHandlerResolver, feedbackValueWriterResolver);
		}
	}

	public void setListener(MidiMappingListener midiMappingListener)
	{
		final MidiDevice device = midi.getDevice();
		// Remove old listener
		if(device != null)
		{
			device.getPublisher().removeMidiListener(this.midiMappingListener);
		}

		this.midiMappingListener = midiMappingListener;

		// Register new listener
		if(device != null)
		{
			device.getPublisher().addMidiListener(midiMappingListener);
		}
	}
}
