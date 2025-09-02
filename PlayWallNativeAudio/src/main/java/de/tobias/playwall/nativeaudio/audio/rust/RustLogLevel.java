package de.tobias.playwall.nativeaudio.audio.rust;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RustLogLevel
{
	TRACE("Trace"),
	DEBUG("Debug"),
	INFO("Info"),
	WARN("Warn"),
	ERROR("Error");

	private final String level;
}
