package de.tobias.playwall.client.domain.midi.device;

import de.thecodelabs.midi.midi.device.MidiListener;
import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MidiDeviceRegistry
{
	private final Map<String, CustomMidiDevice> midiListeners = new HashMap<>();

	@PostConstruct
	private void init()
	{
		midiListeners.put("LPMiniMK3 MIDI Out", new LPMiniMK3());
	}

	public Optional<MidiListener> lookup(String name)
	{
		return Optional.ofNullable(midiListeners.get(name));
	}
}
