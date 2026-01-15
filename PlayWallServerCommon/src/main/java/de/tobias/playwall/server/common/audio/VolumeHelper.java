package de.tobias.playwall.server.common.audio;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VolumeHelper
{
	public static final double MIN_VOLUME = 0.0;
	public static final double MAX_VOLUME = 1.15;
	private static final double MAX_VOLUME_BOOST_IN_DECIBEL = 3.0;
	private static final double SILENCE_IN_DECIBEL = -60.0;

	public static void validateVolume(double volume)
	{
		if(volume < VolumeHelper.MIN_VOLUME || volume > VolumeHelper.MAX_VOLUME)
		{
			throw new IllegalArgumentException(MessageFormat.format("Volume must be between {0} and {1}", VolumeHelper.MIN_VOLUME, VolumeHelper.MAX_VOLUME));
		}
	}

	public static double convertVolumeToLogarithmic(double volume)
	{
		if(volume <= 0)
		{
			return 0;
		}

		double normalizedVolume = volume / MAX_VOLUME;
		double db = SILENCE_IN_DECIBEL + (MAX_VOLUME_BOOST_IN_DECIBEL - SILENCE_IN_DECIBEL) * normalizedVolume;

		return Math.pow(10.0, db / 20.0);
	}
}
