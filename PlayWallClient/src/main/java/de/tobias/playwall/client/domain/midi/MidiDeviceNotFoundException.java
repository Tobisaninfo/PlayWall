package de.tobias.playwall.client.domain.midi;

import lombok.Getter;

@Getter
public class MidiDeviceNotFoundException extends RuntimeException
{
	private final String deviceName;

	public MidiDeviceNotFoundException(String deviceName)
	{
		super("Cannot find midi device: " + deviceName);
		this.deviceName = deviceName;
	}
}
