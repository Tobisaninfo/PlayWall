package de.tobias.playwall.client.appcontext.loader.test.inject.constructor.dependencyMissing;

import de.tobias.playwall.client.appcontext.InjectConstructor;
import de.tobias.playwall.client.appcontext.Service;

@Service
public class TestConstructorDependencyNotRegistered
{
	public static class Dependency
	{

	}

	@InjectConstructor
	public TestConstructorDependencyNotRegistered(Dependency dependency)
	{
	}
}
