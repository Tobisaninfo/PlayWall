package de.tobias.playwall.client.domain.midi.device;

import de.tobias.playwall.client.appcontext.PostConstruct;
import de.tobias.playwall.client.appcontext.Service;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPMK2;
import de.tobias.playwall.client.domain.midi.device.launchpad.LPMiniMK3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomMidiDeviceRegistry
{
	private final Map<String, CustomMidiDevice> midiListeners = new HashMap<>();

	@PostConstruct
	private void init()
	{
		midiListeners.put("LPMiniMK3 MIDI", new LPMiniMK3());
		midiListeners.put("Launchpad MK2", new LPMK2());
	}

	public Optional<CustomMidiDevice> lookup(String name)
	{
		return Optional.ofNullable(midiListeners.get(name));
	}
}
