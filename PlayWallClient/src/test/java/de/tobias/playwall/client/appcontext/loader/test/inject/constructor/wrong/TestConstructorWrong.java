package de.tobias.playwall.client.appcontext.loader.test.inject.constructor.wrong;

import de.tobias.playwall.client.appcontext.Service;

@Service
public class TestConstructorWrong
{
	// Missing component annotation
	public static class Dependency
	{

	}

	// Missing @InjectConstructor annotation
	public TestConstructorWrong(Dependency dependency)
	{
	}
}
