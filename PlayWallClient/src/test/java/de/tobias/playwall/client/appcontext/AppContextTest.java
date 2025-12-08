package de.tobias.playwall.client.appcontext;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppContextTest
{
	private static class Test1
	{
	}

	private static class Test2
	{
	}

	@Test
	void testRegisterLazy()
	{
		AppContext context = new AppContext(AppContext.Environment.TESTING);
		context.registerLazy(Test1.class, _ -> new Test1());

		assertThat(context.get(Test1.class)).isNotNull().isNotSameAs(context.get(Test1.class));
	}

	@Test
	void testRegisterLazyWithMultipleClasses()
	{
		AppContext context = new AppContext(AppContext.Environment.TESTING);
		context.registerLazy(Test1.class, _ -> new Test1());
		context.registerLazy(Test2.class, _ -> new Test2());

		assertThat(context.get(Test1.class)).isNotNull().isNotSameAs(context.get(Test1.class));
		assertThat(context.get(Test2.class)).isNotNull().isNotSameAs(context.get(Test2.class));
	}

	@Test
	void testRegisterLazySingleton()
	{
		AppContext context = new AppContext(AppContext.Environment.TESTING);
		context.registerLazySingleton(Test1.class, _ -> new Test1());

		assertThat(context.get(Test1.class)).isNotNull().isSameAs(context.get(Test1.class));
	}

	@Test
	void testGetWithUnregisteredClass()
	{
		AppContext context = new AppContext(AppContext.Environment.TESTING);

		assertThatThrownBy(() -> context.get(Test1.class))
				.isInstanceOf(ComponentNotFoundException.class)
				.hasMessage("No component found for \"%s\"", Test1.class.getName());
	}
}
