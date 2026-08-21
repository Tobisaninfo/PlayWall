package de.tobias.playwall.client.domain.midi;

import de.thecodelabs.midi.midi.Midi;
import de.tobias.playwall.client.appcontext.Bean;
import de.tobias.playwall.client.appcontext.Configuration;
import de.tobias.playwall.client.appcontext.PostConstruct;

@Configuration
class MidiConfiguration
{
	private Midi midi;

	@PostConstruct
	private void init()
	{
		midi = new Midi();
	}

	@Bean
	public Midi midi()
	{
		return midi;
	}
}
