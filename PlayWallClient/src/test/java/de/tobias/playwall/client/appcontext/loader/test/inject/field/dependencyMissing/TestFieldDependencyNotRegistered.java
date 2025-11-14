package de.tobias.playwall.client.appcontext.loader.test.inject.field.dependencyMissing;

import de.tobias.playwall.client.appcontext.InjectField;
import de.tobias.playwall.client.appcontext.Service;

@Service
public class TestFieldDependencyNotRegistered
{
	public static class Dependency
	{

	}

	@InjectField
	Dependency dependency;
}
