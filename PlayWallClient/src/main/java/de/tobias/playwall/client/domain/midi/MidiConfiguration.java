package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.Midi;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.domain.midi.device.MidiDeviceRegistry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@InjectConstructor}, access = AccessLevel.PACKAGE)
class MidiConfiguration
{
	private final MidiDeviceRegistry midiDeviceRegistry;

	private Midi midi;

	@PostConstruct
	private void init()
	{
		midi = new Midi();
		midi.addMidiListener(new DeviceConfigurationMidiListener(midiDeviceRegistry));
	}

	@Bean
	public Midi midi()
	{
		return midi;
	}
}
