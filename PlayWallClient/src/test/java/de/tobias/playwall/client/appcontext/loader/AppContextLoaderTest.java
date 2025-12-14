package de.tobias.playwall.client.appcontext.loader;

import de.tobias.playwall.client.appcontext.AppContext;
import de.tobias.playwall.client.appcontext.ComponentInitializationException;
import de.tobias.playwall.client.appcontext.loader.test.good.TestInheritance;
import de.tobias.playwall.client.appcontext.loader.test.good.TestInterface;
import de.tobias.playwall.client.appcontext.loader.test.good.TestSingleton;
import de.tobias.playwall.client.appcontext.loader.test.good.TestViewController;
import de.tobias.playwall.client.appcontext.loader.test.inject.constructor.wrong.TestConstructorWrong;
import de.tobias.playwall.client.appcontext.loader.test.inject.constructor.dependencyMissing.TestConstructorDependencyNotRegistered;
import de.tobias.playwall.client.appcontext.loader.test.inject.field.dependencyMissing.TestFieldDependencyNotRegistered;
import de.tobias.playwall.client.extensions.LoggerSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(LoggerSetup.class)
class AppContextLoaderTest
{
	@Test
	void testSetupDependenciesGoodCase()
	{
		final AppContext context = new AppContext();
		AppContextLoader.setupDependencies(context, "de.tobias.playwall.client.appcontext.loader.test.good");

		// Check singleton
		assertThat(context.get(TestSingleton.class)).isNotNull()
				.isSameAs(context.get(TestSingleton.class));

		// Check inheritance
		assertThat(context.get(TestInterface.class)).isNotNull()
				.isInstanceOf(TestInheritance.class)
				.isNotSameAs(context.get(TestInterface.class));

		// Check view controller
		assertThat(context.get(TestViewController.class)).isNotNull()
				.isNotSameAs(context.get(TestViewController.class));

		// Check injection
		assertThat(context.get(TestViewController.class).getSingleton()).isNotNull()
				.isSameAs(context.get(TestSingleton.class));
		assertThat(context.get(TestViewController.class).getAnInterface()).isNotNull()
				.isNotSameAs(context.get(TestInterface.class));
	}

	@Test
	void testSetupDependenciesInjectConstructorNotPresent()
	{
		final AppContext context = new AppContext();
		assertThatThrownBy(() -> AppContextLoader.setupDependencies(context, "de.tobias.playwall.client.appcontext.loader.test.inject.constructor.wrong"))
				.isInstanceOf(ComponentInitializationException.class)
				.hasMessage("No suitable constructor found for class %s", TestConstructorWrong.class.getName())
				.hasCauseInstanceOf(NoSuchMethodException.class);
	}

	@Test
	void testSetupDependenciesInjectConstructorDependencyNotRegistered()
	{
		final AppContext context = new AppContext();
		AppContextLoader.setupDependencies(context, "de.tobias.playwall.client.appcontext.loader.test.inject.constructor.dependencyMissing");
		assertThatThrownBy(() -> context.get(TestConstructorDependencyNotRegistered.class))
				.isInstanceOf(ComponentInitializationException.class)
				.hasMessage("Cannot instantiate component class %s", TestConstructorDependencyNotRegistered.class.getName());
	}

	@Test
	void testSetupDependenciesInjectFieldDependencyNotRegistered()
	{
		final AppContext context = new AppContext();
		AppContextLoader.setupDependencies(context, "de.tobias.playwall.client.appcontext.loader.test.inject.field.dependencyMissing");
		assertThatThrownBy(() -> context.get(TestFieldDependencyNotRegistered.class))
				.isInstanceOf(ComponentInitializationException.class)
				.hasMessage("Cannot instantiate component class %s", TestFieldDependencyNotRegistered.class.getName());
	}
}
