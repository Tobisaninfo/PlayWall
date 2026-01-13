package de.tobias.playwall.server.common.audio;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VolumeHelper
{
	public static final double MIN_VOLUME = 0.0;
	public static final double MAX_VOLUME = 1.15;

	public static void validateVolume(double volume)
	{
		if(volume < VolumeHelper.MIN_VOLUME || volume > VolumeHelper.MAX_VOLUME)
		{
			throw new IllegalArgumentException(MessageFormat.format("Volume must be between {0} and {1}", VolumeHelper.MIN_VOLUME, VolumeHelper.MAX_VOLUME));
		}
	}
}
