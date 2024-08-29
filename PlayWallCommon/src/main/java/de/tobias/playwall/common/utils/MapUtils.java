package de.tobias.playwall.common.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils
{
	private MapUtils()
	{
	}

	public static class MapEntry
	{
		private final String key;
		private final String value;

		MapEntry(String key, String value)
		{
			this.key = key;
			this.value = value;
		}
	}

	public static Map<String, String> create(MapEntry... entries)
	{
		Map<String, String> map = new HashMap<>();
		for(MapEntry entry : entries)
		{
			map.put(entry.key, entry.value);
		}

		return map;
	}

	public static MapEntry entry(String key, String value)
	{
		return new MapEntry(key, value);
	}
}
