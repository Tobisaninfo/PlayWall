package de.tobias.playwall.server.common.audio;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VolumeHelper
{
	public static final double MIN_VOLUME = 0.0;
	public static final double MAX_VOLUME = 1.25;

	public static void validateVolume(double volume)
	{
		if(volume < VolumeHelper.MIN_VOLUME || volume > VolumeHelper.MAX_VOLUME)
		{
			throw new IllegalArgumentException(MessageFormat.format("Volume must be between {0} and {1}", VolumeHelper.MIN_VOLUME, VolumeHelper.MAX_VOLUME));
		}
	}

	/**
	 * Converts the linear volume input value to a perceived loudness.
	 * 0.0 --> 0.0
	 * 0.5 --> 0.25
	 * 1.0 --> 1.0
	 * 1.25 --> 1.56
	 */
	public static double convertVolumeToLogarithmic(double volume)
	{
		if(volume <= 0)
		{
			return 0;
		}

		return Math.pow(volume, 2);
	}
}
