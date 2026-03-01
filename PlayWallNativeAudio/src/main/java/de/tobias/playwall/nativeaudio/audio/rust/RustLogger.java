package de.tobias.playwall.nativeaudio.audio.rust;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RustLogger
{
	public static void logFromRust(int level, String msg)
	{
		switch(level)
		{
			case 1 -> log.error(msg);
			case 2 -> log.warn(msg);
			case 4 -> log.debug(msg);
			case 5 -> log.trace(msg);
			default -> log.info(msg);
		}
	}
}
