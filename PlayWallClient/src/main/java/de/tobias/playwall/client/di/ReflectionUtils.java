package de.tobias.playwall.client.di;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils
{
	public static List<Field> getAllFields(Class<?> type)
	{
		final List<Field> fields = new ArrayList<>();
		Class<?> current = type;

		while(current != null && current != Object.class)
		{
			fields.addAll(Arrays.asList(current.getDeclaredFields()));
			current = current.getSuperclass();
		}
		return fields;
	}
}
