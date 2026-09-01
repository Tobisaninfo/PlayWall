package de.tobias.playwall.nativeaudio.audio.rust;

import java.util.Objects;

public record AudioDevice(String name, int channels, int sampleRate, boolean defaultDevice, String driver)
{
	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(!(o instanceof AudioDevice that)) return false;
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
