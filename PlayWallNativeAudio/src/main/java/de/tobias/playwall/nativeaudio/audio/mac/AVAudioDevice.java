package de.tobias.playwall.nativeaudio.audio.mac;

import java.util.Objects;

public record AVAudioDevice(String name, String vendor, String id)
{
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(!(o instanceof AVAudioDevice that)) return false;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name);
	}

	@Override
	public String toString()
	{
		return name;
	}
}
