package de.tobias.playwall.server.common.audio;

import de.thecodelabs.utils.util.Localization;

import java.util.ArrayList;
import java.util.List;

public class AudioCapability
{
	public static final AudioCapability SOUNDCARD = new AudioCapability("SOUNDCARD", Soundcardable.class);

	private final String nameKey;
	private final Class<? extends AudioFeature> clazz;

	private static final List<AudioCapability> audioCapabilityList;

	static
	{
		audioCapabilityList = new ArrayList<>();
		audioCapabilityList.add(SOUNDCARD);
	}

	private AudioCapability(String nameKey, Class<? extends AudioFeature> clazz)
	{
		this.nameKey = nameKey;
		this.clazz = clazz;
	}

	public String getNameKey()
	{
		return nameKey;
	}

	public String getName()
	{
		return Localization.getString(getNameKey());
	}

	public Class<? extends AudioFeature> getAudioFeature()
	{
		return clazz;
	}

	public static List<AudioCapability> getFeatures()
	{
		return audioCapabilityList;
	}

}
