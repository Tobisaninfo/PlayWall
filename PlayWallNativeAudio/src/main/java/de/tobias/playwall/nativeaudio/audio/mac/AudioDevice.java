package de.tobias.playwall.nativeaudio.audio.mac;

import java.util.Objects;

public class AudioDevice {
	private final String name;
	private final String vendor;
	private final String id;

	public AudioDevice(String name, String vendor, String id) {
		this.name = name;
		this.vendor = vendor;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getVendor() {
		return vendor;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AudioDevice)) return false;
		AudioDevice that = (AudioDevice) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
