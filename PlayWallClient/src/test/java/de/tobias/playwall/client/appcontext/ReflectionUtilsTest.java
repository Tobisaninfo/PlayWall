package de.tobias.playwall.client.appcontext;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class ReflectionUtilsTest
{
	private static class A
	{
		private int a;
	}

	private static class B extends A
	{
		private int b;
	}

	private static class C extends B
	{
		private int c;
	}

	@Test
	void testGetAllFields()
	{
		final List<Field> list = ReflectionUtils.getAllFields(C.class);

		assertThat(list).hasSize(3)
				.extracting(Field::getName)
				.containsExactlyInAnyOrder("a", "b", "c");

	}
}
